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
    Set<Entity> targetEntities =
        world.getEntitiesWithComponents(
            List.of(Velocity.class, CollisionEvent.class, CollisionEvent.class));
    for (Entity entity : targetEntities) {
      Velocity velocity = entity.getComponent(Velocity.class).orElseThrow();
      CollisionEvent event = entity.getComponent(CollisionEvent.class).orElseThrow();
      boolean isBouncing = entity.getComponent(Bounciness.class).isPresent();
      world.removeComponentFromEntity(entity, velocity);
      entity.addComponent( // TODO: refine velocity update logic based on collision direction
          new Velocity(isBouncing ? -velocity.dx() : 0, isBouncing ? -velocity.dy() : 0));
      world.removeComponentFromEntity(entity, event);
      if (event.otherEntity().getComponent(GroundComponent.class).isPresent()) {
        entity.addComponent(new GroundedEvent());
      }
    }
  }
}
