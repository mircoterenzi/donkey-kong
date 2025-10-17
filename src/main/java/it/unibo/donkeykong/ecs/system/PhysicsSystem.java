package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.List;
import java.util.Set;

/**
 * PhysicsSystem is responsible for handling physics-related updates in the game world, such as a
 * moving entity trying to pass through a static obstacle.
 */
public class PhysicsSystem implements GameSystem {

  @Override
  public void update(World world, long deltaTime) {
    Set<Entity> movingEntities =
        world.getEntitiesWithComponents(List.of(Velocity.class, CollisionEvent.class));
    for (Entity movingEntity : movingEntities) {
      Velocity velocity = movingEntity.getComponent(Velocity.class).orElseThrow();
      CollisionEvent event = movingEntity.getComponent(CollisionEvent.class).orElseThrow();
      boolean isBouncing = movingEntity.getComponent(Bounciness.class).isPresent();

      world.removeComponentFromEntity(movingEntity, velocity);
      world.removeComponentFromEntity(movingEntity, event);
      world.removeComponentFromEntity(event.otherEntity(), event);
      movingEntity.addComponent(
          new Velocity(isBouncing ? -velocity.dx() : 0, isBouncing ? -velocity.dy() : 0));
    }
  }
}
