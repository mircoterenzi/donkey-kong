package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** HealthSystem manages the health status of entities within the game world. */
public class HealthSystem implements GameSystem {

  @Override
  public void update(World world, long deltaTime) {
    Set<Entity> targetEntities =
        world.getEntitiesWithComponents(List.of(Health.class, CollisionEvent.class));
    for (Entity entity : targetEntities) {
      Health health = entity.getComponent(Health.class).orElseThrow();
      int currentLives = health.livesCount();
      List<Entity> damagingEntities =
          entity.getComponent(CollisionEvent.class).orElseThrow().getCollisionsWith(Damage.class);
      int totalDamage =
          damagingEntities.stream()
              .map(otherEntity -> otherEntity.getComponent(Damage.class))
              .map(Optional::orElseThrow)
              .map(Damage::damageAmount)
              .reduce(0, Integer::sum);
      int newLives = currentLives - totalDamage;
      if (newLives > 0) {
        entity.updateComponent(health, new Health(newLives));
      } else {
        world.removeEntity(entity);
      }
    }
  }
}
