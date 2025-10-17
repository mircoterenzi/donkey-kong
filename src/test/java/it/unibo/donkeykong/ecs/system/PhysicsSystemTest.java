package it.unibo.donkeykong.ecs.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.WorldImpl;
import it.unibo.donkeykong.ecs.component.Bounciness;
import it.unibo.donkeykong.ecs.component.CollisionEvent;
import it.unibo.donkeykong.ecs.component.Velocity;
import it.unibo.donkeykong.ecs.entity.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PhysicsSystemTest {

  private static final long DELTA_TIME_IGNORED = 0L;
  private static final int VELOCITY_X = 50;
  private static final int VELOCITY_Y = 75;
  private static final int ZERO_VELOCITY = 0;

  private World world;
  private Entity obstacle;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    world.addSystem(new PhysicsSystem());
    obstacle = world.createEntity();
  }

  private Entity createCollidingEntity() {
    return world
        .createEntity()
        .addComponent(new Velocity(VELOCITY_X, VELOCITY_Y))
        .addComponent(new CollisionEvent(obstacle));
  }

  private Velocity getVelocityComponent(Entity entity) {
    return entity
        .getComponent(Velocity.class)
        .orElseThrow(() -> new AssertionError("Velocity component missing"));
  }

  @Test
  void testCollidingEntityWithoutBouncinessStops() {
    Entity entity = createCollidingEntity();
    world.update(DELTA_TIME_IGNORED);
    assertEquals(
        new Velocity(ZERO_VELOCITY, ZERO_VELOCITY),
        getVelocityComponent(entity),
        "Entity's velocity should be zero after collision if not bouncing.");
    assertFalse(entity.getComponent(CollisionEvent.class).isPresent(), "Event should be consumed.");
  }

  @Test
  void testCollidingEntityWithBouncinessReversesVelocity() {
    Entity entity = createCollidingEntity().addComponent(new Bounciness());
    world.update(DELTA_TIME_IGNORED);
    assertEquals(
        new Velocity(-VELOCITY_X, -VELOCITY_Y),
        getVelocityComponent(entity),
        "Entity's velocity should be inverted (-V.X, -V.Y) after collision.");
    assertTrue(
        entity.getComponent(Bounciness.class).isPresent(), "Bounciness component should persist.");
    assertFalse(entity.getComponent(CollisionEvent.class).isPresent(), "Event should be consumed.");
  }

  @Test
  void testNonCollidingEntityIsUnaffected() {
    Entity entity = world.createEntity();
    entity.addComponent(new Velocity(VELOCITY_X, VELOCITY_Y));
    world.update(DELTA_TIME_IGNORED);
    assertEquals(
        new Velocity(VELOCITY_X, VELOCITY_Y),
        getVelocityComponent(entity),
        "Non-colliding entity's velocity should remain unchanged.");
  }
}
