package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import java.util.List;

/**
 * PhysicsSystem is responsible for handling physics-related updates in the game world, such as a
 * moving entity trying to pass through a static obstacle.
 */
public class PhysicsSystem extends CollisionEventHandlerSystem {

  @Override
  public void update(World world, long deltaTime) {
    super.handleCollision(
        world,
        List.of(Velocity.class, CollisionEvent.class),
        (entity) -> {
          Velocity velocity = entity.getComponent(Velocity.class).orElseThrow();
          boolean isBouncing = entity.getComponent(Bounciness.class).isPresent();
          world.removeComponentFromEntity(entity, velocity);
          entity.addComponent(
              new Velocity(isBouncing ? -velocity.dx() : 0, isBouncing ? -velocity.dy() : 0));
        });
  }
}
