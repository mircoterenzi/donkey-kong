package it.unibo.donkeykong.ecs.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.WorldImpl;
import it.unibo.donkeykong.ecs.component.Component;
import it.unibo.donkeykong.ecs.component.Position;
import it.unibo.donkeykong.ecs.component.Velocity;
import it.unibo.donkeykong.ecs.entity.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MovementSystemTest {

  private static final long DELTA_TIME_MILLISECONDS = 1000L;
  private static final float DELTA_TIME_SECONDS = DELTA_TIME_MILLISECONDS / 1000f;

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

  private static Position getPositionComponent(Entity entity) {
    return entity
        .getComponent(Position.class)
        .orElseThrow(() -> new AssertionError("Position component not present in the entity"));
  }

  @Test
  void testEntityWithPositionAndVelocityMovesCorrectly() {
    Velocity velocity = new Velocity(POSITIVE_VELOCITY_X, POSITIVE_VELOCITY_Y);
    Position initialPosition = new Position(INITIAL_X, INITIAL_Y);
    Position expectedPosition =
        new Position(
            INITIAL_X + (int) (POSITIVE_VELOCITY_X * DELTA_TIME_SECONDS),
            INITIAL_Y + (int) (POSITIVE_VELOCITY_Y * DELTA_TIME_SECONDS));
    Entity entity = world.createEntity();
    world.addComponentToEntity(entity, initialPosition);
    world.addComponentToEntity(entity, velocity);
    world.update(DELTA_TIME_MILLISECONDS);
    Position actualPosition = getPositionComponent(entity);
    assertEquals(expectedPosition, actualPosition, "Entity did not move to the expected position.");
    assertNotSame(initialPosition, actualPosition, "Position component must be a new instance.");
    assertFalse(
        world.getComponentsOfEntity(entity).contains(initialPosition),
        "Initial position component must be removed.");
  }

  @Test
  void testEntityWithNegativeVelocityMovesCorrectly() {
    Velocity velocity = new Velocity(NEGATIVE_VELOCITY_X, NEGATIVE_VELOCITY_Y);
    Position initialPosition = new Position(INITIAL_X, INITIAL_Y);
    Position expectedPosition =
        new Position(
            INITIAL_X + (int) (NEGATIVE_VELOCITY_X * DELTA_TIME_SECONDS),
            INITIAL_Y + (int) (NEGATIVE_VELOCITY_Y * DELTA_TIME_SECONDS));
    Entity entity = world.createEntity();
    world.addComponentToEntity(entity, initialPosition);
    world.addComponentToEntity(entity, velocity);
    world.update(DELTA_TIME_MILLISECONDS);
    Position actualPosition = getPositionComponent(entity);
    assertEquals(
        expectedPosition,
        actualPosition,
        "Entity did not move to the expected position with negative velocity.");
  }

  @Test
  void testEntityWithoutVelocityDoesNotMove() {
    Position initialPosition = new Position(INITIAL_X, INITIAL_Y);
    Entity entity = world.createEntity();
    world.addComponentToEntity(entity, initialPosition);
    world.addComponentToEntity(entity, new TestComponent());
    world.update(DELTA_TIME_MILLISECONDS);
    Position actualPosition = getPositionComponent(entity);
    assertEquals(
        initialPosition,
        actualPosition,
        "Entity position must remain unchanged without Velocity component.");
    assertTrue(
        world.getComponentsOfEntity(entity).contains(initialPosition),
        "Initial position instance must be present if no movement occurred.");
  }

  @Test
  void testZeroVelocityStaysInPlace() {
    Position initialPosition = new Position(INITIAL_X, INITIAL_Y);
    Velocity velocity = new Velocity(ZERO_VELOCITY, ZERO_VELOCITY);
    Entity entity = world.createEntity();
    world.addComponentToEntity(entity, initialPosition);
    world.addComponentToEntity(entity, velocity);
    world.update(DELTA_TIME_MILLISECONDS);
    Position actualPosition = getPositionComponent(entity);
    assertEquals(INITIAL_X, actualPosition.x());
    assertEquals(INITIAL_Y, actualPosition.y());
    assertNotSame(
        initialPosition,
        actualPosition,
        "System must replace component even if values are identical.");
  }

  @Test
  void testEntityLosingComponentStopsMoving() {
    Position position = new Position(ORIGIN, ORIGIN);
    Velocity velocity = new Velocity(POSITIVE_VELOCITY_X, ZERO_VELOCITY);
    Entity entity = world.createEntity();
    world.addComponentToEntity(entity, position);
    world.addComponentToEntity(entity, velocity);
    world.update(DELTA_TIME_MILLISECONDS);
    Position positionAfterFirstMove = getPositionComponent(entity);
    world.removeComponentFromEntity(entity, velocity);
    world.update(DELTA_TIME_MILLISECONDS);
    Position posAfterSecondMove = getPositionComponent(entity);
    assertEquals(
        positionAfterFirstMove.x(),
        posAfterSecondMove.x(),
        "Position must not change after Velocity component removal.");
  }
}
