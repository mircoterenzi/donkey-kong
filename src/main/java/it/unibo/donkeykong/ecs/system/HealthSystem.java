package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** HealthSystem manages the health status of entities within the game world. */
public class HealthSystem implements GameSystem {

  @Override
  public void update(World world, float deltaTime) {
    Set<Entity> targetEntities =
        world.getEntitiesWithComponents(
            List.of(HealthComponent.class, CollisionEventComponent.class));
    for (Entity entity : targetEntities) {
      HealthComponent health = entity.getComponent(HealthComponent.class).orElseThrow();
      int currentLives = health.livesCount();
      List<Entity> damagingEntities =
          entity
              .getComponent(CollisionEventComponent.class)
              .orElseThrow()
              .getCollisionsWith(DamageComponent.class);
      int totalDamage =
          damagingEntities.stream()
              .map(otherEntity -> otherEntity.getComponent(DamageComponent.class))
              .map(Optional::orElseThrow)
              .map(DamageComponent::damageAmount)
              .reduce(0, Integer::sum);
      int newLives = currentLives - totalDamage;
      if (newLives > 0) {
        entity.updateComponent(health, new HealthComponent(newLives));
      } else {
        world.removeEntity(entity);
      }
    }
  }
}
