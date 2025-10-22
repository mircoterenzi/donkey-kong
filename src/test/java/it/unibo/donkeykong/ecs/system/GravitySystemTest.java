package it.unibo.donkeykong.ecs.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.WorldImpl;
import it.unibo.donkeykong.ecs.component.Gravity;
import it.unibo.donkeykong.ecs.component.Position;
import it.unibo.donkeykong.ecs.component.Velocity;
import it.unibo.donkeykong.ecs.entity.Entity;
import it.unibo.donkeykong.utilities.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GravitySystemTest {

  private static final long DELTA_TIME_ONE_SECOND = 1000L;
  private static final long DELTA_TIME_HALF_SECOND = 500L;
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
  void testEntityWithAllComponentsIsAffectedByGravity() {
    Velocity initialVelocity = new Velocity(INITIAL_DX, INITIAL_DY);
    Entity entity =
        world
            .createEntity()
            .addComponent(new Gravity())
            .addComponent(initialVelocity)
            .addComponent(new Position(0, 0));

    world.update(DELTA_TIME_ONE_SECOND);

    Velocity newVelocity = getVelocityComponent(entity);

    assertNotEquals(
        initialVelocity.dy(), newVelocity.dy(), "Vertical velocity should change due to gravity.");
    assertEquals(
        initialVelocity.dx(), newVelocity.dx(), "Horizontal velocity should remain unchanged.");
    assertNotSame(
        initialVelocity,
        newVelocity,
        "A new Velocity component instance should have been created.");
    assertFalse(
        world.getComponentsOfEntity(entity).contains(initialVelocity),
        "The old Velocity component should have been removed.");
  }

  @Test
  void testGravityCalculationIsCorrect() {
    Velocity initialVelocity = new Velocity(INITIAL_DX, INITIAL_DY);
    Entity entity =
        world
            .createEntity()
            .addComponent(new Gravity())
            .addComponent(initialVelocity)
            .addComponent(new Position(0, 0));

    world.update(DELTA_TIME_HALF_SECOND);

    double expectedDy = INITIAL_DY + Constants.GRAVITY * (DELTA_TIME_HALF_SECOND / 1000.0);
    Velocity newVelocity = getVelocityComponent(entity);

    assertEquals(
        expectedDy,
        newVelocity.dy(),
        FLOATING_POINT_DELTA,
        "The new vertical velocity is not calculated correctly.");
    assertEquals(
        INITIAL_DX,
        newVelocity.dx(),
        FLOATING_POINT_DELTA,
        "Horizontal velocity should not change.");
  }

  @Test
  void testEntityWithoutGravityIsNotAffected() {
    Velocity initialVelocity = new Velocity(INITIAL_DX, INITIAL_DY);
    Entity entity =
        world.createEntity().addComponent(initialVelocity).addComponent(new Position(0, 0));

    world.update(DELTA_TIME_ONE_SECOND);

    Velocity newVelocity = getVelocityComponent(entity);
    assertEquals(
        initialVelocity,
        newVelocity,
        "Velocity should not change if Gravity component is missing.");
    assertSame(
        initialVelocity, newVelocity, "The component instance should not have been replaced.");
  }

  @Test
  void testEntityWithoutVelocityIsIgnored() {
    Entity entity =
        world.createEntity().addComponent(new Gravity()).addComponent(new Position(0, 0));

    // L'azione principale è verificare che l'aggiornamento non lanci un'eccezione
    world.update(DELTA_TIME_ONE_SECOND);

    assertTrue(
        entity.getComponent(Velocity.class).isEmpty(),
        "Entity should not gain a Velocity component.");
  }

  @Test
  void testEntityWithoutPositionIsIgnored() {
    Velocity initialVelocity = new Velocity(INITIAL_DX, INITIAL_DY);
    Entity entity = world.createEntity().addComponent(new Gravity()).addComponent(initialVelocity);

    world.update(DELTA_TIME_ONE_SECOND);

    Velocity newVelocity = getVelocityComponent(entity);
    assertEquals(
        initialVelocity,
        newVelocity,
        "Velocity should not change if Position component is missing.");
    assertSame(
        initialVelocity, newVelocity, "The component instance should not have been replaced.");
  }
}
