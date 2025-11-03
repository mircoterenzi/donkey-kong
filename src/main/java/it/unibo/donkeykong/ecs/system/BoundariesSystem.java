package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import it.unibo.donkeykong.utilities.Constants;
import java.util.List;

public class BoundariesSystem implements GameSystem {

  @Override
  public void update(World world, float deltaTime) {
    for (Entity entity : world.getEntitiesWithComponents(List.of(Position.class, Collider.class))) {
      final Position position = entity.getComponent(Position.class).orElseThrow();
      final Collider collider = entity.getComponent(Collider.class).orElseThrow();
      final double halfWidth = collider.width() / 2.0;
      final double halfHeight = collider.height() / 2.0;
      final double newX =
          Math.min(Math.max(position.x(), halfWidth), Constants.WORLD_WIDTH - halfWidth);
      final double newY =
          Math.min(Math.max(position.y(), halfHeight), Constants.WORLD_HEIGHT - halfHeight);

      if (newX != position.x() || newY != position.y()) {
        entity.updateComponent(position, new Position(newX, newY));
        if (entity.getComponent(Bounciness.class).isPresent()) {
          entity
              .getComponent(Velocity.class)
              .ifPresent(
                  velocity ->
                      entity.updateComponent(
                          velocity,
                          new Velocity(
                              (newX != position.x() ? -1 : 1) * velocity.dx(),
                              (newY != position.y() ? -1 : 1) * velocity.dy())));
        }
      }
    }
  }
}
