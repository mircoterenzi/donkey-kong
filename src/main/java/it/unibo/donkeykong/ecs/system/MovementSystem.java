package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.PositionComponent;
import it.unibo.donkeykong.ecs.component.VelocityComponent;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** MovementSystem updates the position of entities based on their velocity over time. */
public class MovementSystem implements GameSystem {

  private static final double MAX_VELOCITY = 200;

  @Override
  public void update(World world, float deltaTime) {
    Set<Entity> entitiesToMove =
        world.getEntitiesWithComponents(List.of(PositionComponent.class, VelocityComponent.class));

    for (Entity entity : entitiesToMove) {
      Optional<PositionComponent> startingPosition = entity.getComponent(PositionComponent.class);
      Optional<VelocityComponent> velocity = entity.getComponent(VelocityComponent.class);

      if (startingPosition.isPresent() && velocity.isPresent()) {
        // TODO: this should limit the max y-speed to impede tunneling (suboptimal).
        if (velocity.get().dy() >= MAX_VELOCITY) {
          VelocityComponent updatedVelocity =
              new VelocityComponent(velocity.get().dx(), MAX_VELOCITY);
          entity.updateComponent(updatedVelocity);
          velocity = Optional.of(updatedVelocity);
        }
        double updatedX = startingPosition.get().x() + (velocity.get().dx() * deltaTime);
        double updatedY = startingPosition.get().y() + (velocity.get().dy() * deltaTime);

        world.updateComponentOnEntity(entity, new PositionComponent(updatedX, updatedY));
      }
    }
  }
}
