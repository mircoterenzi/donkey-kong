package it.unibo.donkeykong.ecs.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.donkeykong.core.WorldImpl;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PhysicsSystemTest {

  private static final long DELTA_TIME = 1L;
  private static final RectangleCollider COLLIDER = new RectangleCollider(15, 18);
  private static final PositionComponent POSITION = new PositionComponent(0, 0);
  private static final double UNCOLLIDING_OFFSET = 20.0;
  private static final double COLLIDING_OFFSET = 10.0;
  private static final VelocityComponent VELOCITY = new VelocityComponent(10.0, 5.0);

  private World world;
  private Entity entity, obstacle;

  @BeforeEach
  void setUp() {
    this.world = new WorldImpl();
    this.world.addSystem(new PhysicsSystem());
    this.entity = this.world.createEntity().addComponent(VELOCITY).addComponent(COLLIDER);
    this.obstacle =
        this.world
            .createEntity()
            .addComponent(POSITION)
            .addComponent(COLLIDER)
            .addComponent(new SolidComponent());
  }

  private void assertEntitiesPositionEquals(PositionComponent expected, Entity actual) {
    Optional<PositionComponent> actualComponent = actual.getComponent(PositionComponent.class);
    assertTrue(actualComponent.isPresent());
    assertEquals(expected, actualComponent.get());
  }

  @Test
  void testNonCollidingEntityNotAltered() {
    PositionComponent position =
        new PositionComponent(POSITION.x(), POSITION.y() + UNCOLLIDING_OFFSET);
    this.entity.addComponent(position);
    this.world.update(DELTA_TIME);
    assertEntitiesPositionEquals(position, this.entity);
    assertEntitiesPositionEquals(POSITION, this.obstacle);
  }

  @Test
  void testVerticalCollidingEntityPositionAdjusted() {
    this.entity
        .addComponent(new PositionComponent(POSITION.x(), POSITION.y() - COLLIDING_OFFSET))
        .addComponent(new CollisionEventComponent(this.obstacle));
    this.world.update(DELTA_TIME);
    assertEntitiesPositionEquals(
        new PositionComponent(POSITION.x(), POSITION.y() - COLLIDER.height()), this.entity);
  }

  @Test
  void testHorizontalCollidingEntityPositionAdjusted() {
    this.entity
        .addComponent(new PositionComponent(POSITION.x() - COLLIDING_OFFSET, POSITION.y()))
        .addComponent(new CollisionEventComponent(this.obstacle));
    this.world.update(DELTA_TIME);
    assertEntitiesPositionEquals(
        new PositionComponent(POSITION.x() - COLLIDER.width(), POSITION.x()), this.entity);
  }

  @Test
  void testMultipleAxisCollidingEntityPositionAdjusted() {
    double lessCollidingOffset = COLLIDING_OFFSET - 1;
    this.entity
        .addComponent(
            new PositionComponent(
                POSITION.x() - lessCollidingOffset, POSITION.y() - COLLIDING_OFFSET))
        .addComponent(new CollisionEventComponent(this.obstacle));
    this.world.update(DELTA_TIME);
    PositionComponent position = this.entity.getComponent(PositionComponent.class).orElseThrow();
    assertEntitiesPositionEquals(
        new PositionComponent(POSITION.x() - COLLIDER.width(), position.y()), this.entity);
  }
}
