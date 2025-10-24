package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.List;
import java.util.Set;

/** HealthSystem manages the health status of entities within the game world. */
public class HealthSystem implements GameSystem {

  @Override
  public void update(World world, long deltaTime) {
    Set<Entity> targetEntities =
        world.getEntitiesWithComponents(
            List.of(Health.class, CollisionEvent.class, CollisionEvent.class));
    for (Entity entity : targetEntities) {
      Health health = entity.getComponent(Health.class).orElseThrow();
      CollisionEvent event = entity.getComponent(CollisionEvent.class).orElseThrow();
      Entity otherEntity = event.otherEntity();
      if (otherEntity.getComponent(Damage.class).isPresent()) {
        Damage damage = otherEntity.getComponent(Damage.class).get();
        world.removeComponentFromEntity(entity, health);
        entity.addComponent(
            new Health(health.livesCount() - damage.damageAmount())); // TODO: handle player death
      }
      world.removeComponentFromEntity(entity, event);
    }
  }
}
