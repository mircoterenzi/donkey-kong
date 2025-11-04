package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.core.Constants.RESPAWN_POSITION;

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
    targetEntities.stream()
        .filter(
            entity -> {
              CollisionEventComponent collisionEvent =
                  entity.getComponent(CollisionEventComponent.class).orElseThrow();
              return collisionEvent.hasCollisionsWith(DamageComponent.class);
            })
        .forEach(
            entity -> {
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
                entity
                    .getComponent(PositionComponent.class)
                    .ifPresent(position -> entity.updateComponent(position, RESPAWN_POSITION));
              } else {
                world.removeEntity(entity);
              }
              damagingEntities.forEach(world::removeEntity);
            });
  }
}
