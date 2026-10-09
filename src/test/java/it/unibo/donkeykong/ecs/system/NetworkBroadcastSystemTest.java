package it.unibo.donkeykong.ecs.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import it.unibo.donkeykong.core.WorldImpl;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.HealthComponent;
import it.unibo.donkeykong.ecs.component.NetworkComponent;
import it.unibo.donkeykong.ecs.component.PositionComponent;
import it.unibo.donkeykong.ecs.component.StateComponent;
import it.unibo.donkeykong.network.protocol.MessageType;
import it.unibo.donkeykong.network.protocol.Net;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class NetworkBroadcastSystemTest {

  @Test
  void testHostBroadcastsStateAndBarrels(Vertx vertx, VertxTestContext testContext) {
    World world = new WorldImpl();
    world.addSystem(new NetworkBroadcastSystem(vertx.eventBus(), "HOST"));

    // Crea l'entità HOST
    world
        .createEntity()
        .addComponent(new NetworkComponent("host-1", "HOST"))
        .addComponent(new PositionComponent(100.0, 50.0))
        .addComponent(
            new StateComponent(StateComponent.State.MOVING, StateComponent.Direction.RIGHT))
        .addComponent(new HealthComponent(3));

    // Crea un Barile
    world
        .createEntity()
        .addComponent(new NetworkComponent("barrel-1", "BARREL"))
        .addComponent(new PositionComponent(200.0, 300.0));

    vertx
        .eventBus()
        .<JsonObject>consumer(
            Net.OUTBOUND,
            msg -> {
              testContext.verify(
                  () -> {
                    JsonObject body = msg.body();
                    assertEquals(MessageType.HOST_UPDATE.name(), body.getString("type"));
                    assertEquals(100.0, body.getDouble("playerX"));
                    assertEquals(3, body.getInteger("lives"));
                    assertEquals(1, body.getJsonArray("barrels").size());
                    assertEquals(
                        "barrel-1", body.getJsonArray("barrels").getJsonObject(0).getString("id"));
                    testContext.completeNow();
                  });
            });

    world.update(0.1f);
  }

  @Test
  void testGuestBroadcastsState(Vertx vertx, VertxTestContext testContext) {
    World world = new WorldImpl();
    world.addSystem(new NetworkBroadcastSystem(vertx.eventBus(), "GUEST"));

    world
        .createEntity()
        .addComponent(new NetworkComponent("guest-1", "GUEST"))
        .addComponent(new PositionComponent(50.0, 20.0))
        .addComponent(new StateComponent(StateComponent.State.JUMP, StateComponent.Direction.LEFT))
        .addComponent(new HealthComponent(2));

    vertx
        .eventBus()
        .<JsonObject>consumer(
            Net.OUTBOUND,
            msg -> {
              testContext.verify(
                  () -> {
                    JsonObject body = msg.body();
                    assertEquals(MessageType.GUEST_UPDATE.name(), body.getString("type"));
                    assertEquals(50.0, body.getDouble("playerX"));
                    assertEquals("JUMP", body.getString("playerState"));
                    assertEquals("LEFT", body.getString("playerDirection"));
                    testContext.completeNow();
                  });
            });

    world.update(0.1f);
  }
}
