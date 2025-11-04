package it.unibo.donkeykong.ecs.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.unibo.donkeykong.core.Constants;
import it.unibo.donkeykong.core.WorldImpl;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.PositionComponent;
import it.unibo.donkeykong.ecs.component.RectangleCollider;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BoundariesSystemTest {

  private static final long DELTA_TIME_IGNORED = 0L;
  private static final int SIZE = 10;
  private static final int DEFAULT_WIDTH = Constants.WORLD_WIDTH / 2;
  private static final int TOO_HIGH_HEIGHT = Constants.WORLD_HEIGHT + SIZE;
  private static final int TOO_LOW_HEIGHT = -SIZE;

  private World world;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    world.addSystem(new BoundariesSystem());
  }

  private Entity createEntity(PositionComponent position) {
    return world
        .createEntity()
        .addComponent(position)
        .addComponent(new RectangleCollider(SIZE, SIZE));
  }

  @Test
  void testRectangleEntityClampedToMinBounds() {
    Entity entity = createEntity(new PositionComponent(DEFAULT_WIDTH, TOO_LOW_HEIGHT));
    int expectedY = SIZE / 2;
    world.update(DELTA_TIME_IGNORED);
    PositionComponent finalPos = entity.getComponent(PositionComponent.class).orElseThrow();
    assertEquals(
        new PositionComponent(DEFAULT_WIDTH, expectedY),
        finalPos,
        "Entity is not correctly updated if is out of min bounds.");
  }

  @Test
  void testRectangleEntityClampedToMaxBounds() {
    Entity entity = createEntity(new PositionComponent(DEFAULT_WIDTH, TOO_HIGH_HEIGHT));
    int expectedY = Constants.WORLD_HEIGHT - (SIZE / 2);
    world.update(DELTA_TIME_IGNORED);
    PositionComponent position = entity.getComponent(PositionComponent.class).orElseThrow();
    assertEquals(
        new PositionComponent(DEFAULT_WIDTH, expectedY),
        position,
        "Entity is not correctly updated if is out of max bounds.");
  }
}
