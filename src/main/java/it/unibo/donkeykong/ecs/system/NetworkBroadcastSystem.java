package it.unibo.donkeykong.ecs.system;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.NetworkComponent;
import it.unibo.donkeykong.ecs.component.PositionComponent;
import it.unibo.donkeykong.ecs.component.StateComponent;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import it.unibo.donkeykong.network.messages.BarrelData;
import it.unibo.donkeykong.network.messages.GuestUpdateMessage;
import it.unibo.donkeykong.network.messages.HostUpdateMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NetworkBroadcastSystem implements GameSystem {
  private final EventBus eventBus;
  private final String myRole;

  public NetworkBroadcastSystem(EventBus eventBus, String myRole) {
    this.eventBus = eventBus;
    this.myRole = myRole;
  }

  @Override
  public void update(World world, float deltaTime) {
    if ("HOST".equals(myRole)) {
      broadcastHostState(world);
    } else if ("GUEST".equals(myRole)) {
      broadcastGuestState(world);
    }
  }

  private void broadcastHostState(World world) {
    Optional<Entity> hostPlayer = findNetworkEntity(world, "HOST");
    if (hostPlayer.isEmpty()) return;

    PositionComponent pos = hostPlayer.get().getComponent(PositionComponent.class).orElseThrow();
    StateComponent state = hostPlayer.get().getComponent(StateComponent.class).orElseThrow();

    List<BarrelData> barrelDataList = new ArrayList<>();
    world
        .getEntitiesWithComponents(List.of(NetworkComponent.class, PositionComponent.class))
        .forEach(
            entity -> {
              NetworkComponent net = entity.getComponent(NetworkComponent.class).orElseThrow();
              if ("BARREL".equals(net.entityType())) {
                PositionComponent bPos = entity.getComponent(PositionComponent.class).orElseThrow();
                barrelDataList.add(new BarrelData(net.networkId(), bPos.x(), bPos.y()));
              }
            });

    HostUpdateMessage msg =
        new HostUpdateMessage(
            pos.x(), pos.y(), state.state().name(), state.direction().name(), barrelDataList);

    eventBus.send("outbound.messages", JsonObject.mapFrom(msg));
  }

  private void broadcastGuestState(World world) {
    Optional<Entity> guestPlayer = findNetworkEntity(world, "GUEST");
    if (guestPlayer.isEmpty()) return;

    PositionComponent pos = guestPlayer.get().getComponent(PositionComponent.class).orElseThrow();
    StateComponent state = guestPlayer.get().getComponent(StateComponent.class).orElseThrow();

    GuestUpdateMessage msg =
        new GuestUpdateMessage(pos.x(), pos.y(), state.state().name(), state.direction().name());

    eventBus.send("outbound.messages", JsonObject.mapFrom(msg));
  }

  private Optional<Entity> findNetworkEntity(World world, String type) {
    return world.getEntitiesWithComponents(List.of(NetworkComponent.class)).stream()
        .filter(
            e -> {
              NetworkComponent net = e.getComponent(NetworkComponent.class).orElseThrow();
              return type.equals(net.entityType());
            })
        .findFirst();
  }
}
