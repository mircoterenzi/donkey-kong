package it.unibo.donkeykong.ecs.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.donkeykong.core.WorldImpl;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.PositionComponent;
import it.unibo.donkeykong.ecs.component.VelocityComponent;
import it.unibo.donkeykong.ecs.component.api.Component;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MovementSystemTest {

  private static final float DELTA_TIME_SECONDS = 1000f;

  private static final int INITIAL_X = 10;
  private static final int INITIAL_Y = 20;
  private static final int ORIGIN = 0;

  private static final int POSITIVE_VELOCITY_X = 100;
  private static final int POSITIVE_VELOCITY_Y = 50;
  private static final int NEGATIVE_VELOCITY_X = -200;
  private static final int NEGATIVE_VELOCITY_Y = -50;
  private static final int ZERO_VELOCITY = 0;

  private World world;

  private static class TestComponent implements Component {}

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    world.addSystem(new MovementSystem());
  }

  private static PositionComponent getPositionComponent(Entity entity) {
    return entity
        .getComponent(PositionComponent.class)
        .orElseThrow(
            () -> new AssertionError("PositionComponent component not present in the entity"));
  }

  @Test
  void testEntityWithPositionAndVelocityMovesCorrectly() {
    VelocityComponent velocity = new VelocityComponent(POSITIVE_VELOCITY_X, POSITIVE_VELOCITY_Y);
    PositionComponent initialPosition = new PositionComponent(INITIAL_X, INITIAL_Y);
    PositionComponent expectedPosition =
        new PositionComponent(
            INITIAL_X + (int) (POSITIVE_VELOCITY_X * DELTA_TIME_SECONDS),
            INITIAL_Y + (int) (POSITIVE_VELOCITY_Y * DELTA_TIME_SECONDS));
    Entity entity = world.createEntity();
    world.addComponentToEntity(entity, initialPosition);
    world.addComponentToEntity(entity, velocity);
    world.update(DELTA_TIME_SECONDS);
    PositionComponent actualPosition = getPositionComponent(entity);
    assertEquals(expectedPosition, actualPosition, "Entity did not move to the expected position.");
    assertNotSame(
        initialPosition, actualPosition, "PositionComponent component must be a new instance.");
    assertFalse(
        world.getComponentsOfEntity(entity).contains(initialPosition),
        "Initial position component must be removed.");
  }

  @Test
  void testEntityWithNegativeVelocityMovesCorrectly() {
    VelocityComponent velocity = new VelocityComponent(NEGATIVE_VELOCITY_X, NEGATIVE_VELOCITY_Y);
    PositionComponent initialPosition = new PositionComponent(INITIAL_X, INITIAL_Y);
    PositionComponent expectedPosition =
        new PositionComponent(
            INITIAL_X + (int) (NEGATIVE_VELOCITY_X * DELTA_TIME_SECONDS),
            INITIAL_Y + (int) (NEGATIVE_VELOCITY_Y * DELTA_TIME_SECONDS));
    Entity entity = world.createEntity();
    world.addComponentToEntity(entity, initialPosition);
    world.addComponentToEntity(entity, velocity);
    world.update(DELTA_TIME_SECONDS);
    PositionComponent actualPosition = getPositionComponent(entity);
    assertEquals(
        expectedPosition,
        actualPosition,
        "Entity did not move to the expected position with negative velocity.");
  }

  @Test
  void testEntityWithoutVelocityDoesNotMove() {
    PositionComponent initialPosition = new PositionComponent(INITIAL_X, INITIAL_Y);
    Entity entity = world.createEntity();
    world.addComponentToEntity(entity, initialPosition);
    world.addComponentToEntity(entity, new TestComponent());
    world.update(DELTA_TIME_SECONDS);
    PositionComponent actualPosition = getPositionComponent(entity);
    assertEquals(
        initialPosition,
        actualPosition,
        "Entity position must remain unchanged without VelocityComponent component.");
    assertTrue(
        world.getComponentsOfEntity(entity).contains(initialPosition),
        "Initial position instance must be present if no movement occurred.");
  }

  @Test
  void testZeroVelocityStaysInPlace() {
    PositionComponent initialPosition = new PositionComponent(INITIAL_X, INITIAL_Y);
    VelocityComponent velocity = new VelocityComponent(ZERO_VELOCITY, ZERO_VELOCITY);
    Entity entity = world.createEntity();
    world.addComponentToEntity(entity, initialPosition);
    world.addComponentToEntity(entity, velocity);
    world.update(DELTA_TIME_SECONDS);
    PositionComponent actualPosition = getPositionComponent(entity);
    assertEquals(INITIAL_X, actualPosition.x());
    assertEquals(INITIAL_Y, actualPosition.y());
    assertNotSame(
        initialPosition,
        actualPosition,
        "System must replace component even if values are identical.");
  }

  @Test
  void testEntityLosingComponentStopsMoving() {
    PositionComponent position = new PositionComponent(ORIGIN, ORIGIN);
    VelocityComponent velocity = new VelocityComponent(POSITIVE_VELOCITY_X, ZERO_VELOCITY);
    Entity entity = world.createEntity();
    world.addComponentToEntity(entity, position);
    world.addComponentToEntity(entity, velocity);
    world.update(DELTA_TIME_SECONDS);
    PositionComponent positionAfterFirstMove = getPositionComponent(entity);
    world.removeComponentFromEntity(entity, velocity);
    world.update(DELTA_TIME_SECONDS);
    PositionComponent posAfterSecondMove = getPositionComponent(entity);
    assertEquals(
        positionAfterFirstMove.x(),
        posAfterSecondMove.x(),
        "PositionComponent must not change after VelocityComponent component removal.");
  }
}
