package it.unibo.donkeykong.ecs.system;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.WorldImpl;
import it.unibo.donkeykong.ecs.component.CircleCollider;
import it.unibo.donkeykong.ecs.component.Collider;
import it.unibo.donkeykong.ecs.component.CollisionEvent;
import it.unibo.donkeykong.ecs.component.Position;
import it.unibo.donkeykong.ecs.component.RectangleCollider;
import it.unibo.donkeykong.ecs.component.Velocity;
import it.unibo.donkeykong.ecs.entity.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CollisionSystemTest {

  private static final long DELTA_TIME_IGNORED = 0L;
  private static final int POSITION_ZERO = 0;
  private static final int POSITION_MEDIUM = 5;
  private static final int POSITION_FAR = 100;
  private static final int POSITION_TOUCHING = 10;
  private static final int SIZE_SMALL = 5;
  private static final int SIZE_MEDIUM = 10;
  private static final int SIZE_LARGE = 20;
  private static final int VELOCITY_ZERO = 0;

  private World world;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    world.addSystem(new CollisionSystem());
  }

  private Entity createMovingSolidEntity(Position position, Collider collider) {
    return world
        .createEntity()
        .addComponent(position)
        .addComponent(collider)
        .addComponent(new Velocity(VELOCITY_ZERO, VELOCITY_ZERO));
  }

  private void assertCollisionExists(Entity entity, Entity otherEntity) {
    assertTrue(
        entity.getComponent(CollisionEvent.class).isPresent(), "Expected collision for entity.");
    assertTrue(
        otherEntity.getComponent(CollisionEvent.class).isPresent(),
        "Expected collision for other entity.");
  }

  private void assertNoCollision(Entity entity) {
    assertFalse(
        entity.getComponent(CollisionEvent.class).isPresent(), "No collision expected for entity.");
  }

  @Test
  void testRectangleRectangleTouching() {
    Entity entity1 =
        createMovingSolidEntity(
            new Position(POSITION_ZERO, POSITION_ZERO),
            new RectangleCollider(SIZE_MEDIUM, SIZE_MEDIUM));
    Entity entity2 =
        createMovingSolidEntity(
            new Position(POSITION_TOUCHING, POSITION_ZERO),
            new RectangleCollider(SIZE_MEDIUM, SIZE_MEDIUM));

    world.update(DELTA_TIME_IGNORED);
    assertCollisionExists(entity1, entity2);
  }

  @Test
  void testRectangleRectangleOverlapping() {
    Entity entity1 =
        createMovingSolidEntity(
            new Position(POSITION_ZERO, POSITION_ZERO),
            new RectangleCollider(SIZE_LARGE, SIZE_LARGE));
    Entity entity2 =
        createMovingSolidEntity(
            new Position(POSITION_MEDIUM, POSITION_MEDIUM),
            new RectangleCollider(SIZE_SMALL, SIZE_SMALL));

    world.update(DELTA_TIME_IGNORED);
    assertCollisionExists(entity1, entity2);
  }

  @Test
  void testRectangleRectangleNotColliding() {
    Entity entity1 =
        createMovingSolidEntity(
            new Position(POSITION_ZERO, POSITION_ZERO),
            new RectangleCollider(SIZE_SMALL, SIZE_SMALL));
    createMovingSolidEntity(
      new Position(POSITION_FAR, POSITION_FAR),
      new RectangleCollider(SIZE_SMALL, SIZE_SMALL));

    world.update(DELTA_TIME_IGNORED);
    assertNoCollision(entity1);
  }

  @Test
  void testCircleCircleTouching() {
    Entity entity1 =
        createMovingSolidEntity(
            new Position(POSITION_ZERO, POSITION_ZERO), new CircleCollider(SIZE_MEDIUM));
    Entity entity2 =
        createMovingSolidEntity(
            new Position(SIZE_LARGE, POSITION_ZERO), new CircleCollider(SIZE_MEDIUM));

    world.update(DELTA_TIME_IGNORED);
    assertCollisionExists(entity1, entity2);
  }

  @Test
  void testCircleCircleOverlapping() {
    Entity entity1 =
        createMovingSolidEntity(
            new Position(POSITION_ZERO, POSITION_ZERO), new CircleCollider(SIZE_LARGE));
    Entity entity2 =
        createMovingSolidEntity(
            new Position(SIZE_MEDIUM, POSITION_ZERO), new CircleCollider(SIZE_MEDIUM));

    world.update(DELTA_TIME_IGNORED);
    assertCollisionExists(entity1, entity2);
  }

  @Test
  void testCircleCircleNotColliding() {
    Entity entity1 =
        createMovingSolidEntity(
            new Position(POSITION_ZERO, POSITION_ZERO), new CircleCollider(SIZE_SMALL));
    createMovingSolidEntity(
      new Position(POSITION_FAR, POSITION_FAR), new CircleCollider(SIZE_SMALL));

    world.update(DELTA_TIME_IGNORED);
    assertNoCollision(entity1);
  }

  @Test
  void testRectangleCircleTouchingEdge() {
    Entity entity1 =
        createMovingSolidEntity(
            new Position(POSITION_ZERO, POSITION_ZERO),
            new RectangleCollider(SIZE_LARGE, SIZE_LARGE));
    Entity entity2 =
        createMovingSolidEntity(
            new Position(SIZE_LARGE, POSITION_ZERO), new CircleCollider(SIZE_MEDIUM));

    world.update(DELTA_TIME_IGNORED);
    assertCollisionExists(entity1, entity2);
  }

  @Test
  void testRectangleCircleOverlapping() {
    Entity entity1 =
        createMovingSolidEntity(
            new Position(POSITION_ZERO, POSITION_ZERO),
            new RectangleCollider(SIZE_LARGE, SIZE_LARGE));
    Entity entity2 =
        createMovingSolidEntity(
            new Position(POSITION_MEDIUM, POSITION_ZERO), new CircleCollider(SIZE_MEDIUM));

    world.update(DELTA_TIME_IGNORED);
    assertCollisionExists(entity1, entity2);
  }

  @Test
  void testRectangleCircleCornerTouching() {
    Entity entity1 =
        createMovingSolidEntity(
            new Position(POSITION_ZERO, POSITION_ZERO),
            new RectangleCollider(SIZE_MEDIUM, SIZE_MEDIUM));
    Entity entity2 =
        createMovingSolidEntity(
            new Position(POSITION_TOUCHING, POSITION_ZERO), new CircleCollider(SIZE_SMALL));

    world.update(DELTA_TIME_IGNORED);
    assertCollisionExists(entity1, entity2);
  }

  @Test
  void testRectangleCircleNotColliding() {
    Entity entity1 =
        createMovingSolidEntity(
            new Position(POSITION_ZERO, POSITION_ZERO),
            new RectangleCollider(SIZE_SMALL, SIZE_SMALL));
    createMovingSolidEntity(
      new Position(POSITION_FAR, POSITION_FAR), new CircleCollider(SIZE_SMALL));

    world.update(DELTA_TIME_IGNORED);
    assertNoCollision(entity1);
  }

  @Test
  void testSelfCollisionIgnored() {
    Entity entity =
        createMovingSolidEntity(
            new Position(POSITION_ZERO, POSITION_ZERO),
            new RectangleCollider(SIZE_MEDIUM, SIZE_MEDIUM));

    world.update(DELTA_TIME_IGNORED);
    assertFalse(
        entity.getComponent(CollisionEvent.class).isPresent(), "Self-collision should be ignored.");
  }
}
