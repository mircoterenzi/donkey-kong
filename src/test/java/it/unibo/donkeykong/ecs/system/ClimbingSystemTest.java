package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.utilities.Constants.GRAVITY;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.WorldImpl;
import it.unibo.donkeykong.ecs.component.Climbable;
import it.unibo.donkeykong.ecs.component.CollisionEvent;
import it.unibo.donkeykong.ecs.component.Gravity;
import it.unibo.donkeykong.ecs.component.Position;
import it.unibo.donkeykong.ecs.component.Velocity;
import it.unibo.donkeykong.ecs.entity.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClimbingSystemTest {

  private static final long DELTA_TIME_IGNORED = 0L;
  private static final int SHARED_X = 50;
  private static final int Y_POSITION = 100;

  private World world;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    world.addSystem(new ClimbingSystem());
  }

  @Test
  void testEntityLosesGravityWhenCollidingWithClimbableAtSameX() {
    Entity entityWithGravity = world.createEntity();
    entityWithGravity.addComponent(new Position(SHARED_X, Y_POSITION));
    entityWithGravity.addComponent(new Gravity(GRAVITY));
    entityWithGravity.addComponent(new Velocity(0, 0));
    Entity climbableEntity =
        world
            .createEntity()
            .addComponent(new Position(SHARED_X, Y_POSITION))
            .addComponent(new Climbable());
    entityWithGravity.addComponent(new CollisionEvent(climbableEntity));
    climbableEntity.addComponent(new CollisionEvent(entityWithGravity));

    assertTrue(
        entityWithGravity.getComponent(Gravity.class).isPresent(),
        "entityWithGravity must start with Gravity.");

    world.update(DELTA_TIME_IGNORED);
    assertFalse(
        entityWithGravity.getComponent(Gravity.class).isPresent(),
        "entityWithGravity must lose Gravity component when colliding with Climbable at the same X.");
    assertFalse(
        entityWithGravity.getComponent(CollisionEvent.class).isPresent(),
        "Collision event must be consumed.");
    assertFalse(
        climbableEntity.getComponent(CollisionEvent.class).isPresent(),
        "Collision event must be consumed.");
  }

  @Test
  void testEntityKeepsGravityWhenCollidingWithClimbableAtDifferentX() {
    Entity entityWithGravity =
        world
            .createEntity()
            .addComponent(new Position(SHARED_X, Y_POSITION))
            .addComponent(new Gravity(GRAVITY))
            .addComponent(new Velocity(0, 0));
    Entity climbableEntity =
        world
            .createEntity()
            .addComponent(new Position(SHARED_X + 1, Y_POSITION))
            .addComponent(new Climbable());
    entityWithGravity.addComponent(new CollisionEvent(climbableEntity));
    climbableEntity.addComponent(new CollisionEvent(entityWithGravity));

    world.update(DELTA_TIME_IGNORED);
    assertTrue(
        entityWithGravity.getComponent(Gravity.class).isPresent(),
        "entityWithGravity must keep Gravity component when colliding with Climbable at a different X.");
  }
}
