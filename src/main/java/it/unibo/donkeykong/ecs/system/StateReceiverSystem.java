package it.unibo.donkeykong.ecs.system;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import it.unibo.donkeykong.core.Constants;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.entity.api.EntityFactory;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import it.unibo.donkeykong.network.protocol.BarrelData;
import it.unibo.donkeykong.network.protocol.GuestUpdateMessage;
import it.unibo.donkeykong.network.protocol.HostUpdateMessage;
import it.unibo.donkeykong.network.protocol.MessageType;
import it.unibo.donkeykong.network.protocol.Net;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class StateReceiverSystem implements GameSystem {

  private final Queue<Consumer<World>> pending = new ConcurrentLinkedQueue<>();
  private final List<MessageConsumer<?>> consumers = new ArrayList<>();

  private final EventBus eventBus;
  private final EntityFactory entityFactory;
  private final String myRole;
  private boolean isGuestDisconnected = false;

  public StateReceiverSystem(EventBus eventBus, String myRole, EntityFactory entityFactory) {
    this.myRole = myRole;
    this.entityFactory = entityFactory;
    this.eventBus = eventBus;

    on(MessageType.ENTITY_DESTROYED, this::handleEntityDestroyed);

    if ("HOST".equals(myRole) || "SPECTATOR".equals(myRole)) {
      on(MessageType.GUEST_UPDATE, this::handleGuestUpdate);
      on(MessageType.GUEST_DISCONNECTED, this::handleGuestDisconnect);
      on(MessageType.GUEST_RECONNECTED, this::handleGuestReconnect);
    }

    if ("GUEST".equals(myRole) || "SPECTATOR".equals(myRole)) {
      on(MessageType.HOST_UPDATE, this::handleHostUpdate);
    }

    if ("GUEST".equals(myRole)) {
      on(MessageType.RESTORE_STATE, this::handleRestoreState);
    }
  }

  private void on(MessageType type, BiConsumer<World, JsonObject> action) {
    consumers.add(
        eventBus.<JsonObject>consumer(
            Net.inbound(type), msg -> pending.add(world -> action.accept(world, msg.body()))));
  }

  @Override
  public void update(World world, float deltaTime) {
    for (Consumer<World> action; (action = pending.poll()) != null; ) {
      action.accept(world);
    }
  }

  private void handleGuestDisconnect(World world, JsonObject ignore) {
    isGuestDisconnected = true;
    findByType(world, "GUEST")
        .ifPresent(
            guestEntity -> {
              guestEntity.updateComponent(new VelocityComponent(0, 0));
              guestEntity.updateComponent(
                  new StateComponent(StateComponent.State.IDLE, StateComponent.Direction.RIGHT));
            });
  }

  private void handleGuestReconnect(World world, JsonObject ignore) {
    isGuestDisconnected = false;

    if ("HOST".equals(myRole)) {
      findByType(world, "GUEST")
          .ifPresent(
              guestEntity -> {
                double x = guestEntity.getComponent(PositionComponent.class).orElseThrow().x();
                double y = guestEntity.getComponent(PositionComponent.class).orElseThrow().y();
                int lives =
                    guestEntity.getComponent(HealthComponent.class).orElseThrow().livesCount();

                JsonObject restoreMsg =
                    new JsonObject()
                        .put("type", "RESTORE_STATE")
                        .put("playerX", x)
                        .put("playerY", y)
                        .put("lives", lives);
                eventBus.send(Net.OUTBOUND, restoreMsg);
              });
    }
  }

  private void handleRestoreState(World world, JsonObject restoreStateData) {
    findByType(world, "GUEST")
        .ifPresent(
            guestEntity -> {
              guestEntity.updateComponent(
                  new PositionComponent(
                      restoreStateData.getDouble("playerX"),
                      restoreStateData.getDouble("playerY")));
              guestEntity.updateComponent(
                  new HealthComponent(restoreStateData.getInteger("lives")));
            });
  }

  private void handleEntityDestroyed(World world, JsonObject data) {
    String idToDestroy = data.getString("id");
    world.getEntitiesWithComponents(List.of(NetworkComponent.class)).stream()
        .filter(
            e ->
                idToDestroy.equals(
                    e.getComponent(NetworkComponent.class).orElseThrow().networkId()))
        .forEach(world::removeEntity);
  }

  private void handleGuestUpdate(World world, JsonObject update) {
    if (isGuestDisconnected) return;
    GuestUpdateMessage msg = update.mapTo(GuestUpdateMessage.class);
    applyPlayerUpdate(
        world,
        "GUEST",
        msg.playerX(),
        msg.playerY(),
        msg.playerState(),
        msg.playerDirection(),
        msg.lives());
  }

  private void handleHostUpdate(World world, JsonObject update) {
    HostUpdateMessage msg = update.mapTo(HostUpdateMessage.class);

    applyPlayerUpdate(
        world,
        "HOST",
        msg.playerX(),
        msg.playerY(),
        msg.playerState(),
        msg.playerDirection(),
        msg.lives());

    Set<String> activeBarrelIds = new HashSet<>();
    Map<String, Entity> barrelMap = new HashMap<>();

    world
        .getEntitiesWithComponents(List.of(NetworkComponent.class))
        .forEach(
            e -> {
              NetworkComponent net = e.getComponent(NetworkComponent.class).orElseThrow();
              if ("BARREL".equals(net.entityType())) {
                barrelMap.put(net.networkId(), e);
              }
            });

    List<BarrelData> barrels = msg.barrels();
    if (barrels != null) {
      for (BarrelData barrel : barrels) {
        String barrelId = barrel.id();

        if (!activeBarrelIds.add(barrelId)) {
          continue;
        }

        Entity existingBarrel = barrelMap.get(barrelId);
        PositionComponent position = new PositionComponent(barrel.x(), barrel.y());
        if (existingBarrel != null) {
          existingBarrel.updateComponent(position);
        } else {
          double defaultVelocity =
              barrel.x() > Constants.WORLD_WIDTH / 2.0
                  ? -Constants.BARREL_VELOCITY
                  : Constants.BARREL_VELOCITY;
          entityFactory.createNetworkBarrel(barrelId, position, defaultVelocity);
        }
      }
    }

    if ("GUEST".equals(myRole) || "SPECTATOR".equals(myRole)) {
      barrelMap.forEach(
          (id, entity) -> {
            if (!activeBarrelIds.contains(id)) {
              world.removeEntity(entity);
            }
          });
    }
  }

  private void applyPlayerUpdate(
      World world, String type, double x, double y, String state, String direction, int lives) {
    findByType(world, type)
        .ifPresent(
            entity -> {
              entity.updateComponent(new PositionComponent(x, y));
              entity.updateComponent(
                  new StateComponent(
                      StateComponent.State.valueOf(state),
                      StateComponent.Direction.valueOf(direction)));
              entity.updateComponent(new HealthComponent(lives));
            });
  }

  private Optional<Entity> findByType(World world, String type) {
    return world.getEntitiesWithComponents(List.of(NetworkComponent.class)).stream()
        .filter(e -> type.equals(e.getComponent(NetworkComponent.class).orElseThrow().entityType()))
        .findFirst();
  }

  public void stop() {
    consumers.forEach(MessageConsumer::unregister);
  }
}
