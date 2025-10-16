package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;

import java.util.List;

/**
 * GravitySystem applies gravitational force to entities with Gravity and Velocity components.
 */
public class GravitySystem implements GameSystem{
  @Override
  public void update(World world, long deltaTime) {
    world.getEntitiesWithComponents(List.of(Gravity.class, Velocity.class, Position.class))
      .forEach(entity -> {
        var gravity = entity.getComponent(Gravity.class).orElseThrow();
        var velocity = entity.getComponent(Velocity.class).orElseThrow();
        var position = entity.getComponent(Position.class).orElseThrow();

        // Update velocity based on gravity
        double newVy = velocity.dy() + gravity * (deltaTime / 1000.0);
        Velocity newVelocity = new Velocity(velocity.dx(), newVy);
        entity.addComponent(newVelocity);
      });
  }
}
