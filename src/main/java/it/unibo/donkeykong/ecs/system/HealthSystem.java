package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.List;

/** HealthSystem manages the health status of entities within the game world. */
public class HealthSystem extends CollisionEventHandlerSystem {

  @Override
  public void update(World world, long deltaTime) {
    super.handleCollision(
        world,
        List.of(Health.class, CollisionEvent.class),
        (entity) -> {
          Health health = entity.getComponent(Health.class).orElseThrow();
          Entity otherEntity =
              entity.getComponent(CollisionEvent.class).orElseThrow().otherEntity();
          if (otherEntity.getComponent(Damage.class).isPresent()) {
            Damage damage = otherEntity.getComponent(Damage.class).get();
            world.removeComponentFromEntity(entity, health);
            entity.addComponent(
                new Health(
                    health.livesCount() - damage.damageAmount())); // TODO: handle player death
          }
        });
  }
}
