package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.Position;
import it.unibo.donkeykong.ecs.component.Velocity;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** MovementSystem updates the position of entities based on their velocity over time. */
public class MovementSystem implements GameSystem {

  private static final double MAX_VELOCITY = 200;

  @Override
  public void update(World world, float deltaTime) {
    Set<Entity> entitiesToMove =
        world.getEntitiesWithComponents(List.of(Position.class, Velocity.class));

    for (Entity entity : entitiesToMove) {
      Optional<Position> startingPosition = entity.getComponent(Position.class);
      Optional<Velocity> velocity = entity.getComponent(Velocity.class);

      if (startingPosition.isPresent() && velocity.isPresent()) {
        // TODO: this should limit the max y-speed to impede tunneling (suboptimal).
        if (velocity.get().dy() >= MAX_VELOCITY) {
          Velocity updatedVelocity = new Velocity(velocity.get().dx(), MAX_VELOCITY);
          entity.updateComponent(velocity.get(), updatedVelocity);
          velocity = Optional.of(updatedVelocity);
        }
        double updatedX = startingPosition.get().x() + (velocity.get().dx() * deltaTime);
        double updatedY = startingPosition.get().y() + (velocity.get().dy() * deltaTime);

        world.removeComponentFromEntity(entity, startingPosition.get());
        world.addComponentToEntity(entity, new Position(updatedX, updatedY));
      }
    }
  }
}
