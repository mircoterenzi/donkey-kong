package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.core.Constants.GRAVITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.donkeykong.core.WorldImpl;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.GravityComponent;
import it.unibo.donkeykong.ecs.component.VelocityComponent;
import it.unibo.donkeykong.ecs.entity.api.Entity;
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

  private VelocityComponent getVelocityComponent(Entity entity) {
    return entity
        .getComponent(VelocityComponent.class)
        .orElseThrow(
            () -> new AssertionError("VelocityComponent component not present in the entity"));
  }

  @Test
  void testEntityWithGravityAndVelocityIsAffected() {
    VelocityComponent initialVelocity = new VelocityComponent(INITIAL_DX, INITIAL_DY);
    Entity entity =
        world
            .createEntity()
            .addComponent(new GravityComponent(GRAVITY))
            .addComponent(initialVelocity);

    world.update(DELTA_TIME_IGNORED);

    VelocityComponent newVelocity = getVelocityComponent(entity);

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
        "A new VelocityComponent component instance should have been created.");
  }

  @Test
  void testEntityWithoutGravityIsIgnored() {
    VelocityComponent initialVelocity = new VelocityComponent(INITIAL_DX, INITIAL_DY);
    Entity entity = world.createEntity().addComponent(initialVelocity);

    world.update(DELTA_TIME_IGNORED);

    VelocityComponent newVelocity = getVelocityComponent(entity);

    assertEquals(
        INITIAL_DY,
        newVelocity.dy(),
        FLOATING_POINT_DELTA,
        "VelocityComponent dy should be unchanged if GravityComponent component is missing.");
    assertEquals(
        INITIAL_DX,
        newVelocity.dx(),
        FLOATING_POINT_DELTA,
        "VelocityComponent dx should be unchanged.");

    assertSame(
        initialVelocity, newVelocity, "The component instance should NOT have been replaced.");
  }

  @Test
  void testEntityWithoutVelocityIsIgnored() {
    Entity entity = world.createEntity().addComponent(new GravityComponent(GRAVITY));

    world.update(DELTA_TIME_IGNORED);

    assertTrue(
        entity.getComponent(VelocityComponent.class).isEmpty(),
        "Entity should not gain a VelocityComponent component.");
  }

  @Test
  void testEntityWithoutPositionIsAffected() {
    VelocityComponent initialVelocity = new VelocityComponent(INITIAL_DX, INITIAL_DY);
    Entity entity =
        world
            .createEntity()
            .addComponent(new GravityComponent(GRAVITY))
            .addComponent(initialVelocity);

    world.update(DELTA_TIME_IGNORED);

    VelocityComponent newVelocity = getVelocityComponent(entity);
    assertEquals(
        INITIAL_DY + GRAVITY,
        newVelocity.dy(),
        FLOATING_POINT_DELTA,
        "VelocityComponent should change even if PositionComponent component is missing.");
    assertNotSame(
        initialVelocity, newVelocity, "The component instance should have been replaced.");
  }
}
