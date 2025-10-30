package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.utilities.Constants.GRAVITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.WorldImpl;
import it.unibo.donkeykong.ecs.component.Gravity;
import it.unibo.donkeykong.ecs.component.Velocity;
import it.unibo.donkeykong.ecs.entity.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GravitySystemTest {

  private static final long DELTA_TIME_IGNORED = 0L;
  private static final double INITIAL_DX = 10.0;
  private static final double INITIAL_DY = 5.0;
  private static final double FLOATING_POINT_DELTA = 0.001;

  private World world;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    world.addSystem(new GravitySystem());
  }

  private Velocity getVelocityComponent(Entity entity) {
    return entity
        .getComponent(Velocity.class)
        .orElseThrow(() -> new AssertionError("Velocity component not present in the entity"));
  }

  @Test
  void testEntityWithGravityAndVelocityIsAffected() {
    Velocity initialVelocity = new Velocity(INITIAL_DX, INITIAL_DY);
    Entity entity =
        world.createEntity().addComponent(new Gravity(GRAVITY)).addComponent(initialVelocity);

    world.update(DELTA_TIME_IGNORED);

    Velocity newVelocity = getVelocityComponent(entity);

    assertEquals(
        INITIAL_DX,
        newVelocity.dx(),
        FLOATING_POINT_DELTA,
        "Horizontal velocity should remain unchanged.");
    assertEquals(
        INITIAL_DY + GRAVITY,
        newVelocity.dy(),
        FLOATING_POINT_DELTA,
        "Vertical velocity should change due to gravity.");
    assertNotSame(
        initialVelocity,
        newVelocity,
        "A new Velocity component instance should have been created.");
  }

  @Test
  void testEntityWithoutGravityIsIgnored() {
    Velocity initialVelocity = new Velocity(INITIAL_DX, INITIAL_DY);
    Entity entity = world.createEntity().addComponent(initialVelocity);

    world.update(DELTA_TIME_IGNORED);

    Velocity newVelocity = getVelocityComponent(entity);

    assertEquals(
        INITIAL_DY,
        newVelocity.dy(),
        FLOATING_POINT_DELTA,
        "Velocity dy should be unchanged if Gravity component is missing.");
    assertEquals(
        INITIAL_DX, newVelocity.dx(), FLOATING_POINT_DELTA, "Velocity dx should be unchanged.");

    assertSame(
        initialVelocity, newVelocity, "The component instance should NOT have been replaced.");
  }

  @Test
  void testEntityWithoutVelocityIsIgnored() {
    Entity entity = world.createEntity().addComponent(new Gravity(GRAVITY));

    world.update(DELTA_TIME_IGNORED);

    assertTrue(
        entity.getComponent(Velocity.class).isEmpty(),
        "Entity should not gain a Velocity component.");
  }

  @Test
  void testEntityWithoutPositionIsAffected() {
    Velocity initialVelocity = new Velocity(INITIAL_DX, INITIAL_DY);
    Entity entity =
        world.createEntity().addComponent(new Gravity(GRAVITY)).addComponent(initialVelocity);

    world.update(DELTA_TIME_IGNORED);

    Velocity newVelocity = getVelocityComponent(entity);
    assertEquals(
        INITIAL_DY + GRAVITY,
        newVelocity.dy(),
        FLOATING_POINT_DELTA,
        "Velocity should change even if Position component is missing.");
    assertNotSame(
        initialVelocity, newVelocity, "The component instance should have been replaced.");
  }
}
