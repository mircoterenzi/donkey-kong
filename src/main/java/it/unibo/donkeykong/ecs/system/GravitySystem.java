package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import java.util.List;

/**
 * GravitySystem applies gravitational force to entities with GravityComponent and VelocityComponent
 * components.
 */
public class GravitySystem implements GameSystem {
  @Override
  public void update(World world, float deltaTime) {
    world
        .getEntitiesWithComponents(List.of(GravityComponent.class, VelocityComponent.class))
        .forEach(
            entity -> {
              var gravity =
                  entity.getComponent(GravityComponent.class).orElse(new GravityComponent(0));
              var velocity = entity.getComponent(VelocityComponent.class).orElseThrow();
              double newDy = velocity.dy() + gravity.gravity();
              VelocityComponent newVelocity = new VelocityComponent(velocity.dx(), newDy);
              entity.updateComponent(newVelocity);
            });
  }
}
