package it.unibo.donkeykong.ecs.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.unibo.donkeykong.core.WorldImpl;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.CollisionEventComponent;
import it.unibo.donkeykong.ecs.component.DamageComponent;
import it.unibo.donkeykong.ecs.component.HealthComponent;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HealthSystemTest {

  private static final long DELTA_TIME_IGNORED = 0L;
  private static final int INITIAL_LIVES = 3;
  private static final int DAMAGE_AMOUNT = 1;
  private static final int EXPECTED_LIVES_AFTER_HIT = INITIAL_LIVES - DAMAGE_AMOUNT;

  private World world;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    world.addSystem(new HealthSystem());
  }

  private HealthComponent getHealthComponent(Entity entity) {
    return entity
        .getComponent(HealthComponent.class)
        .orElseThrow(() -> new AssertionError("HealthComponent component missing after update."));
  }

  @Test
  void testCollisionWithDamageReducesHealth() {
    Entity target =
        world.createEntity().addComponent(new HealthComponent(HealthSystemTest.INITIAL_LIVES));
    Entity damageSource =
        world.createEntity().addComponent(new DamageComponent(HealthSystemTest.DAMAGE_AMOUNT));
    target.addComponent(new CollisionEventComponent(damageSource));
    world.update(DELTA_TIME_IGNORED);
    assertEquals(
        EXPECTED_LIVES_AFTER_HIT,
        getHealthComponent(target).livesCount(),
        "HealthComponent should be reduced by the damage amount.");
  }

  @Test
  void testCollisionWithoutDamageDoesNotReduceHealth() {
    Entity target =
        world.createEntity().addComponent(new HealthComponent(HealthSystemTest.INITIAL_LIVES));
    Entity harmlessSource = world.createEntity();
    target.addComponent(new CollisionEventComponent(harmlessSource));
    world.update(DELTA_TIME_IGNORED);
    assertEquals(
        INITIAL_LIVES,
        getHealthComponent(target).livesCount(),
        "HealthComponent should remain unchanged when colliding with a non-damaging entity.");
  }

  @Test
  void testDamagingEntitiesRemovedOnCollision() {
    Entity target =
        world.createEntity().addComponent(new HealthComponent(HealthSystemTest.INITIAL_LIVES));
    Entity damageSource =
        world.createEntity().addComponent(new DamageComponent(HealthSystemTest.DAMAGE_AMOUNT));
    target.addComponent(new CollisionEventComponent(damageSource));
    world.update(DELTA_TIME_IGNORED);
    assertEquals(0, world.getEntitiesWithComponents(List.of(DamageComponent.class)).size());
  }
}
