package it.unibo.donkeykong.ecs.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.WorldImpl;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PhysicsSystemTest {

  private static final long DELTA_TIME = 1L;
  private static final double VELOCITY_VALUE = 10.0;
  private static final int COLLIDER_SIZE = 15;
  private static final Collider COLLIDER = new RectangleCollider(COLLIDER_SIZE, COLLIDER_SIZE);
  private static final int OBSTACLE_POSITION_COORDINATE = 0;
  private static final int COLLIDED_ENTITY_POSITION_COORDINATE = COLLIDER_SIZE / 2;
  private static final int UNCOLLIDED_ENTITY_POSITION_COORDINATE =
      (int) (COLLIDED_ENTITY_POSITION_COORDINATE - (VELOCITY_VALUE * DELTA_TIME));
  private static final Position DEFAULT_POSITION =
      new Position(OBSTACLE_POSITION_COORDINATE, OBSTACLE_POSITION_COORDINATE);
  private static final Velocity VELOCITY = new Velocity(VELOCITY_VALUE, VELOCITY_VALUE);

  private World world;
  private Entity entity, obstacle;

  @BeforeEach
  void setUp() {
    this.world = new WorldImpl();
    this.world.addSystem(new PhysicsSystem());
    this.entity = this.world.createEntity().addComponent(VELOCITY).addComponent(COLLIDER);
    this.obstacle = this.world.createEntity().addComponent(DEFAULT_POSITION).addComponent(COLLIDER);
  }

  private <C extends Component> void assertEntityComponentEquals(
      Class<C> componentClass, C expected, Entity actual) {
    Optional<C> actualComponent = actual.getComponent(componentClass);
    assertTrue(actualComponent.isPresent());
    assertEquals(expected, actualComponent.get());
  }

  @Test
  void testNonCollidingEntityNotAltered() {
    this.entity.addComponent(DEFAULT_POSITION);
    this.world.update(DELTA_TIME);
    assertEntityComponentEquals(Position.class, DEFAULT_POSITION, this.entity);
    assertEntityComponentEquals(Position.class, DEFAULT_POSITION, this.obstacle);
  }

  @Test
  void testVerticalCollidingEntityPositionAdjusted() {
    this.entity.addComponent(
        new Position(OBSTACLE_POSITION_COORDINATE, COLLIDED_ENTITY_POSITION_COORDINATE));
    this.entity.addComponent(new CollisionEvent(this.obstacle));
    this.world.update(DELTA_TIME);
    assertEntityComponentEquals(
        Position.class,
        new Position(OBSTACLE_POSITION_COORDINATE, UNCOLLIDED_ENTITY_POSITION_COORDINATE),
        this.entity);
  }

  @Test
  void testHorizontalCollidingEntityPositionAdjusted() {
    this.entity.addComponent(
        new Position(COLLIDED_ENTITY_POSITION_COORDINATE, OBSTACLE_POSITION_COORDINATE));
    this.entity.addComponent(new CollisionEvent(this.obstacle));
    this.world.update(DELTA_TIME);
    assertEntityComponentEquals(
        Position.class,
        new Position(UNCOLLIDED_ENTITY_POSITION_COORDINATE, OBSTACLE_POSITION_COORDINATE),
        this.entity);
  }

  @Test
  void testMultipleAxisCollidingEntityPositionAdjusted() {
    this.entity.addComponent(
        new Position(COLLIDED_ENTITY_POSITION_COORDINATE, COLLIDED_ENTITY_POSITION_COORDINATE));
    this.entity.addComponent(new CollisionEvent(this.obstacle));
    this.world.update(DELTA_TIME);
    Position position = this.entity.getComponent(Position.class).orElseThrow();
    assertTrue(
        position.equals(
                new Position(
                    UNCOLLIDED_ENTITY_POSITION_COORDINATE, COLLIDED_ENTITY_POSITION_COORDINATE))
            || position.equals(
                new Position(
                    COLLIDED_ENTITY_POSITION_COORDINATE, UNCOLLIDED_ENTITY_POSITION_COORDINATE)));
  }

  @Test
  void testNonBouncyEntityVelocityUnchangedOnCollision() {
    this.entity.addComponent(
        new Position(COLLIDED_ENTITY_POSITION_COORDINATE, COLLIDED_ENTITY_POSITION_COORDINATE));
    this.entity.addComponent(new CollisionEvent(this.obstacle));
    this.world.update(DELTA_TIME);
    assertEntityComponentEquals(Velocity.class, VELOCITY, this.entity);
  }

  @Test
  void testBouncyEntityVelocityReversedOnCollision() {
    this.entity
        .addComponent(
            new Position(COLLIDED_ENTITY_POSITION_COORDINATE, COLLIDED_ENTITY_POSITION_COORDINATE))
        .addComponent(new Bounciness());
    this.obstacle.addComponent(new Bounciness());
    this.entity.addComponent(new CollisionEvent(this.obstacle));
    this.world.update(DELTA_TIME);
    assertEntityComponentEquals(
        Velocity.class, new Velocity(-VELOCITY_VALUE, -VELOCITY_VALUE), this.entity);
  }
}
