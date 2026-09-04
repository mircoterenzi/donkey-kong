package it.unibo.donkeykong.ecs.system;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.NetworkComponent;
import it.unibo.donkeykong.ecs.component.PositionComponent;
import it.unibo.donkeykong.ecs.component.StateComponent;
import it.unibo.donkeykong.ecs.system.api.GameSystem;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StateReceiverSystem implements GameSystem {

  private final ConcurrentLinkedQueue<JsonObject> hostUpdates = new ConcurrentLinkedQueue<>();
  private final ConcurrentLinkedQueue<JsonObject> guestUpdates = new ConcurrentLinkedQueue<>();

  private final String myRole;

  public StateReceiverSystem(EventBus eventbus, String myRole) {
    this.myRole = myRole;

    eventbus.<JsonObject>consumer("inbound.host_update", msg -> hostUpdates.add(msg.body()));
    eventbus.<JsonObject>consumer("inbound.guest_update", msg -> guestUpdates.add(msg.body()));
  }

  @Override
  public void update(World world, float deltaTime) {
    if ("GUEST".equals(myRole)) {
      while(!hostUpdates.isEmpty()) {
        JsonObject update = hostUpdates.poll();
        applyHostUpdate(world, update);
      }
    }

    if ("HOST".equals(myRole)) {
      while(!guestUpdates.isEmpty()) {
        JsonObject update = guestUpdates.poll();
        applyGuestUpdate(world, update);
      }
    }
  }

  private void applyGuestUpdate(World world, JsonObject update) {
    double x = update.getDouble("playerX");
    double y = update.getDouble("playerY");
    String state = update.getString("playerState");
    String direction = update.getString("playerDirection");

    world.getEntitiesWithComponents(List.of(NetworkComponent.class)).stream()
      .filter(e -> {
        NetworkComponent net = e.getComponent(NetworkComponent.class).orElseThrow();
        return "GUEST".equals(net.entityType());
      })
      .findFirst()
      .ifPresent(guestEntity -> {
        guestEntity.updateComponent(new PositionComponent(x, y));
        guestEntity.updateComponent(new StateComponent(StateComponent.State.valueOf(state),
          StateComponent.Direction.valueOf(direction)));
      });
  }

  private void applyHostUpdate(World world, JsonObject update) {
    double x = update.getDouble("playerX");
    double y = update.getDouble("playerY");
    String state = update.getString("playerState");
    String direction = update.getString("playerDirection");

    world.getEntitiesWithComponents(List.of(NetworkComponent.class)).stream()
      .filter(e -> {
        NetworkComponent net = e.getComponent(NetworkComponent.class).orElseThrow();
        return "HOST".equals(net.entityType());
      })
      .findFirst()
      .ifPresent(hostEntity -> {
        hostEntity.updateComponent(new PositionComponent(x, y));
        hostEntity.updateComponent(new StateComponent(StateComponent.State.valueOf(state),
          StateComponent.Direction.valueOf(direction)));
      });

    JsonArray barrels = update.getJsonArray("barrels");
    if(barrels != null) {
      for (int i = 0; i < barrels.size(); i++) {
        JsonObject barrel = barrels.getJsonObject(i);
        String barrelId = barrel.getString("id");
        double barrelX = barrel.getDouble("x");
        double barrelY = barrel.getDouble("y");

        var existingBarrel = world.getEntitiesWithComponents(List.of(NetworkComponent.class)).stream()
          .filter(e -> {
            NetworkComponent net = e.getComponent(NetworkComponent.class).orElseThrow();
            return barrelId.equals(net.networkId());
          })
          .findFirst();

        if (existingBarrel.isPresent()) {
          existingBarrel.get().updateComponent(new PositionComponent(barrelX, barrelY));
        } else {
          // Entity newBarrel = factory.createBarrel(bX, bY);
          // newBarrel.addComponent(new NetworkComponent(barrelId, "BARREL"));

          System.out.println("Nuovo barile spawnato dall'Host! ID: " + barrelId);
        }
      }
    }
  }
}
