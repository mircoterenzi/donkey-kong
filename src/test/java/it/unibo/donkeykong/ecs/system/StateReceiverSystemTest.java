package it.unibo.donkeykong.ecs.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import it.unibo.donkeykong.core.WorldImpl;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.PositionComponent;
import it.unibo.donkeykong.ecs.entity.EntityFactoryImpl;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class StateReceiverSystemTest {
  private World world;
  private Entity guestPlayer;

  @BeforeEach
  void setUp(Vertx vertx) {
    world = new WorldImpl();
    EntityFactoryImpl factory = new EntityFactoryImpl(world, "SPECTATOR");

    guestPlayer = factory.createSecondPlayer();

    world.addSystem(new StateReceiverSystem(vertx.eventBus(), "SPECTATOR", factory));
  }

  @Test
  void testApplyGuestUpdate(Vertx vertx, VertxTestContext testContext) {
    JsonObject updateMsg =
        new JsonObject()
            .put("playerX", 150.0)
            .put("playerY", 250.0)
            .put("playerState", "MOVING")
            .put("playerDirection", "LEFT")
            .put("lives", 2);

    vertx.eventBus().publish("inbound.guest_update", updateMsg);

    vertx.setTimer(
        100,
        id -> {
          world.update(0.1f);

          PositionComponent pos = guestPlayer.getComponent(PositionComponent.class).orElseThrow();

          testContext.verify(
              () -> {
                assertEquals(150.0, pos.x());
                assertEquals(250.0, pos.y());
                testContext.completeNow();
              });
        });
  }
}
