package it.unibo.donkeykong.core;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.EntityFactoryImpl;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.system.HealthSystem;
import it.unibo.donkeykong.ecs.system.StateReceiverSystem;
import it.unibo.donkeykong.ecs.system.WinSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class EndGameScenariosTest {

  private World world;
  private boolean winTriggered;
  private boolean deathTriggered;

  @BeforeEach
  void setUp(Vertx vertx) {
    world = new WorldImpl();
    winTriggered = false;
    deathTriggered = false;

    world.addSystem(new WinSystem(entity -> winTriggered = true));
    world.addSystem(new HealthSystem(entity -> deathTriggered = true, destroyed -> {}));
    world.addSystem(
        new StateReceiverSystem(vertx.eventBus(), "HOST", new EntityFactoryImpl(world, "HOST")));
  }

  @Test
  void testPrincessRescueWinCondition() {
    Entity player = world.createEntity().addComponent(new InputComponent());
    Entity pauline = world.createEntity().addComponent(new GoalComponent());

    player.addComponent(new CollisionEventComponent(pauline));

    world.update(0.1f);

    assertTrue(
        winTriggered, "La collisione con il GoalComponent deve innescare la callback di vittoria.");
    assertFalse(deathTriggered, "La collisione con l'obiettivo non deve innescare la morte.");
  }

  @Test
  void testDeathByBarrelsCondition() {
    Entity player = world.createEntity().addComponent(new HealthComponent(1));
    Entity barrel = world.createEntity().addComponent(new DamageComponent(1));

    player.addComponent(new CollisionEventComponent(barrel));

    world.update(0.1f);

    assertTrue(deathTriggered, "L'azzeramento della salute deve innescare la callback di morte.");
    assertFalse(winTriggered, "La morte non deve innescare la vittoria.");
    assertTrue(
        world.getComponentsOfEntity(player).isEmpty(),
        "L'entità del giocatore deve essere rimossa dal mondo una volta morto.");
  }

  @Test
  void testGuestDisconnectionHaltsPlayer(Vertx vertx, VertxTestContext testContext) {
    Entity guest =
        world
            .createEntity()
            .addComponent(new NetworkComponent("guest-id", "GUEST"))
            .addComponent(new VelocityComponent(100, 50))
            .addComponent(
                new StateComponent(StateComponent.State.MOVING, StateComponent.Direction.LEFT));

    vertx.eventBus().publish("inbound.guest_disconnected", new JsonObject());

    vertx.setTimer(
        100,
        id -> {
          world.update(0.1f);

          testContext.verify(
              () -> {
                VelocityComponent vel = guest.getComponent(VelocityComponent.class).orElseThrow();
                StateComponent state = guest.getComponent(StateComponent.class).orElseThrow();

                assertEquals(
                    0.0, vel.dx(), "La velocità orizzontale del guest disconnesso deve essere 0.");
                assertEquals(
                    0.0, vel.dy(), "La velocità verticale del guest disconnesso deve essere 0.");
                assertEquals(
                    StateComponent.State.IDLE,
                    state.state(),
                    "Lo stato del guest disconnesso deve essere resettato a IDLE.");
                testContext.completeNow();
              });
        });
  }
}
