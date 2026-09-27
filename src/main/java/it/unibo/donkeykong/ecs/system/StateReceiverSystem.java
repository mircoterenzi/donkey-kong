package it.unibo.donkeykong.ecs.system;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import it.unibo.donkeykong.core.Constants;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.api.EntityFactory;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import it.unibo.donkeykong.network.protocol.GuestUpdateMessage;
import it.unibo.donkeykong.network.protocol.HostUpdateMessage;
import it.unibo.donkeykong.network.protocol.MessageType;
import it.unibo.donkeykong.network.protocol.Net;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StateReceiverSystem implements GameSystem {

  private final ConcurrentLinkedQueue<JsonObject> hostUpdates = new ConcurrentLinkedQueue<>();
  private final ConcurrentLinkedQueue<JsonObject> guestUpdates = new ConcurrentLinkedQueue<>();
  private final ConcurrentLinkedQueue<String> destroyedEntities = new ConcurrentLinkedQueue<>();
  private final List<MessageConsumer<?>> consumers = new java.util.ArrayList<>();

  private volatile boolean processGuestDisconnect = false;
  private volatile boolean processGuestReconnect = false;
  private volatile JsonObject restoreStateDate = null;
  private boolean isGuestDisconnected = false;
  private final EventBus eventBus;
  private final EntityFactory entityFactory;
  private final String myRole;

  public StateReceiverSystem(EventBus eventbus, String myRole, EntityFactory entityFactory) {
    this.myRole = myRole;
    this.entityFactory = entityFactory;
    this.eventBus = eventbus;

    consumers.add(
        eventbus.<JsonObject>consumer(
            Net.inbound(MessageType.HOST_UPDATE), msg -> hostUpdates.add(msg.body())));
    consumers.add(
        eventbus.<JsonObject>consumer(
            Net.inbound(MessageType.GUEST_UPDATE), msg -> guestUpdates.add(msg.body())));
    consumers.add(
        eventbus.<JsonObject>consumer(
            Net.inbound(MessageType.ENTITY_DESTROYED),
            msg -> destroyedEntities.add(msg.body().getString("id"))));
    consumers.add(
        eventbus.<JsonObject>consumer(
            Net.inbound(MessageType.RESTORE_STATE), msg -> restoreStateDate = msg.body()));
    consumers.add(
        eventbus.<JsonObject>consumer(
            Net.inbound(MessageType.GUEST_RECONNECTED), msg -> processGuestReconnect = true));
    consumers.add(
        eventbus.<JsonObject>consumer(
            Net.inbound(MessageType.GUEST_DISCONNECTED), msg -> processGuestDisconnect = true));
  }

  @Override
  public void update(World world, float deltaTime) {
    if (processGuestDisconnect) {
      processGuestDisconnect = false;
      isGuestDisconnected = true;

      if ("HOST".equals(myRole) || "SPECTATOR".equals(myRole)) {
        world.getEntitiesWithComponents(List.of(NetworkComponent.class)).stream()
            .filter(
                e -> {
                  NetworkComponent net = e.getComponent(NetworkComponent.class).orElseThrow();
                  return "GUEST".equals(net.entityType());
                })
            .findFirst()
            .ifPresent(
                guestEntity -> {
                  guestEntity.updateComponent(new VelocityComponent(0, 0));
                  guestEntity.updateComponent(
                      new StateComponent(
                          StateComponent.State.IDLE, StateComponent.Direction.RIGHT));
                });
      }
    }

    if (processGuestReconnect) {
      processGuestReconnect = false;
      isGuestDisconnected = false;

      if ("HOST".equals(myRole)) {
        world
            .getEntitiesWithComponents(
                List.of(NetworkComponent.class, PositionComponent.class, HealthComponent.class))
            .stream()
            .filter(
                e ->
                    "GUEST"
                        .equals(e.getComponent(NetworkComponent.class).orElseThrow().entityType()))
            .findFirst()
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
                  eventBus.send("outbound.messages", restoreMsg);
                });
      }
    }

    if (restoreStateDate != null) {
      if ("GUEST".equals(myRole)) {
        final JsonObject data = restoreStateDate;
        world.getEntitiesWithComponents(List.of(NetworkComponent.class)).stream()
            .filter(
                e ->
                    "GUEST"
                        .equals(e.getComponent(NetworkComponent.class).orElseThrow().entityType()))
            .findFirst()
            .ifPresent(
                guestEntity -> {
                  guestEntity.updateComponent(
                      new PositionComponent(data.getDouble("playerX"), data.getDouble("playerY")));
                  guestEntity.updateComponent(new HealthComponent(data.getInteger("lives")));
                });
      }
      restoreStateDate = null;
    }

    while (!destroyedEntities.isEmpty()) {
      String idToDestroy = destroyedEntities.poll();
      world.getEntitiesWithComponents(List.of(NetworkComponent.class)).stream()
          .filter(
              e -> {
                NetworkComponent net = e.getComponent(NetworkComponent.class).orElseThrow();
                return idToDestroy.equals(net.networkId());
              })
          .forEach(world::removeEntity);
    }

    if ("GUEST".equals(myRole) || "SPECTATOR".equals(myRole)) {
      while (!hostUpdates.isEmpty()) {
        JsonObject update = hostUpdates.poll();
        applyHostUpdate(world, update);
      }
    }

    if ("HOST".equals(myRole) || "SPECTATOR".equals(myRole)) {
      while (!guestUpdates.isEmpty()) {
        JsonObject update = guestUpdates.poll();
        applyGuestUpdate(world, update);
      }
    }
  }

  private void applyGuestUpdate(World world, JsonObject update) {
    if (isGuestDisconnected) return;
    GuestUpdateMessage msg = update.mapTo(GuestUpdateMessage.class);
    double x = msg.playerX();
    double y = msg.playerY();
    String state = msg.playerState();
    String direction = msg.playerDirection();
    int lives = msg.lives();

    world.getEntitiesWithComponents(List.of(NetworkComponent.class)).stream()
        .filter(
            e -> {
              NetworkComponent net = e.getComponent(NetworkComponent.class).orElseThrow();
              return "GUEST".equals(net.entityType());
            })
        .findFirst()
        .ifPresent(
            guestEntity -> {
              guestEntity.updateComponent(new PositionComponent(x, y));
              guestEntity.updateComponent(
                  new StateComponent(
                      StateComponent.State.valueOf(state),
                      StateComponent.Direction.valueOf(direction)));
              guestEntity.updateComponent(new HealthComponent(lives));
            });
  }

  private void applyHostUpdate(World world, JsonObject update) {
    HostUpdateMessage msg = update.mapTo(HostUpdateMessage.class);

    double x = msg.playerX();
    double y = msg.playerY();
    String state = msg.playerState();
    String direction = msg.playerDirection();
    int lives = msg.lives();

    world.getEntitiesWithComponents(List.of(NetworkComponent.class)).stream()
        .filter(
            e -> {
              NetworkComponent net = e.getComponent(NetworkComponent.class).orElseThrow();
              return "HOST".equals(net.entityType());
            })
        .findFirst()
        .ifPresent(
            hostEntity -> {
              hostEntity.updateComponent(new PositionComponent(x, y));
              hostEntity.updateComponent(
                  new StateComponent(
                      StateComponent.State.valueOf(state),
                      StateComponent.Direction.valueOf(direction)));
              hostEntity.updateComponent(new HealthComponent(lives));
            });

    List<it.unibo.donkeykong.network.protocol.BarrelData> barrels = msg.barrels();
    Set<String> activeBarrelIds = new HashSet<>();
    if (barrels != null) {
      for (it.unibo.donkeykong.network.protocol.BarrelData barrel : barrels) {
        String barrelId = barrel.id();
        activeBarrelIds.add(barrelId);
        double barrelX = barrel.x();
        double barrelY = barrel.y();

        var existingBarrel =
            world.getEntitiesWithComponents(List.of(NetworkComponent.class)).stream()
                .filter(
                    e -> {
                      NetworkComponent net = e.getComponent(NetworkComponent.class).orElseThrow();
                      return barrelId.equals(net.networkId());
                    })
                .findFirst();

        if (existingBarrel.isPresent()) {
          existingBarrel.get().updateComponent(new PositionComponent(barrelX, barrelY));
        } else {

          double defaultVelocity =
              barrelX > Constants.WORLD_WIDTH / 2.0
                  ? -Constants.BARREL_VELOCITY
                  : Constants.BARREL_VELOCITY;
          entityFactory.createNetworkBarrel(
              barrelId, new PositionComponent(barrelX, barrelY), defaultVelocity);
        }
      }
    }

    if ("GUEST".equals(myRole) || "SPECTATOR".equals(myRole)) {
      world.getEntitiesWithComponents(List.of(NetworkComponent.class)).stream()
          .filter(
              e -> {
                NetworkComponent net = e.getComponent(NetworkComponent.class).orElseThrow();
                return "BARREL".equals(net.entityType())
                    && !activeBarrelIds.contains(net.networkId());
              })
          .forEach(world::removeEntity);
    }
  }

  public void stop() {
    consumers.forEach(MessageConsumer::unregister);
  }
}
