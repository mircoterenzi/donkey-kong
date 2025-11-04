package it.unibo.donkeykong.ecs.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import it.unibo.donkeykong.core.WorldImpl;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.CollisionEvent;
import it.unibo.donkeykong.ecs.component.Damage;
import it.unibo.donkeykong.ecs.component.Health;
import it.unibo.donkeykong.ecs.entity.api.Entity;
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

  private Health getHealthComponent(Entity entity) {
    return entity
        .getComponent(Health.class)
        .orElseThrow(() -> new AssertionError("Health component missing after update."));
  }

  @Test
  void testCollisionWithDamageReducesHealth() {
    Entity target = world.createEntity().addComponent(new Health(HealthSystemTest.INITIAL_LIVES));
    Entity damageSource =
        world.createEntity().addComponent(new Damage(HealthSystemTest.DAMAGE_AMOUNT));
    target.addComponent(new CollisionEvent(damageSource));
    world.update(DELTA_TIME_IGNORED);
    assertEquals(
        EXPECTED_LIVES_AFTER_HIT,
        getHealthComponent(target).livesCount(),
        "Health should be reduced by the damage amount.");
  }

  @Test
  void testCollisionWithoutDamageDoesNotReduceHealth() {
    Entity target = world.createEntity().addComponent(new Health(HealthSystemTest.INITIAL_LIVES));
    Entity harmlessSource = world.createEntity();
    target.addComponent(new CollisionEvent(harmlessSource));
    world.update(DELTA_TIME_IGNORED);
    assertEquals(
        INITIAL_LIVES,
        getHealthComponent(target).livesCount(),
        "Health should remain unchanged when colliding with a non-damaging entity.");
  }
}
