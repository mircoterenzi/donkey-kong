package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.Position;
import it.unibo.donkeykong.ecs.component.Velocity;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class MovementSystem implements GameSystem {

  @Override
  public void update(World world, long deltaTime) {
    float secondsElapsed = deltaTime / 1000f;
    Set<Entity> entitiesToMove =
        world.getEntitiesWithComponents(List.of(Position.class, Velocity.class));

    for (Entity entity : entitiesToMove) {
      Optional<Position> startingPosition = entity.getComponent(Position.class);
      Optional<Velocity> velocity = entity.getComponent(Velocity.class);

      if (startingPosition.isPresent() && velocity.isPresent()) {
        double updatedX = startingPosition.get().x() + velocity.get().dx() * secondsElapsed;
        double updatedY = startingPosition.get().y() + velocity.get().dy() * secondsElapsed;

        world.removeComponentFromEntity(entity, startingPosition.get());
        world.addComponentToEntity(entity, new Position(updatedX, updatedY));
      }
    }
  }
}
