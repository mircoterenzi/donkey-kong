package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import it.unibo.donkeykong.utilities.Constants;
import java.util.List;

public class BoundariesSystem implements GameSystem {

  @Override
  public void update(World world, long deltaTime) {
    for (Entity entity : world.getEntitiesWithComponents(List.of(Position.class, Collider.class))) {
      Position position = entity.getComponent(Position.class).orElseThrow();
      Collider collider = entity.getComponent(Collider.class).orElseThrow();
      double halfWidth, halfHeight;

      if (collider instanceof RectangleCollider rectangleCollider) {
        halfWidth = rectangleCollider.width() / 2.0;
        halfHeight = rectangleCollider.height() / 2.0;
      } else if (collider instanceof CircleCollider circleCollider) {
        halfWidth = halfHeight = circleCollider.radius();
      } else {
        throw new IllegalArgumentException("Unknown collider type");
      }
      entity.removeComponent(position);
      entity.addComponent(
          new Position(
              Math.min(Math.max(position.x(), halfWidth), Constants.WORLD_WIDTH - halfWidth),
              Math.min(Math.max(position.y(), halfHeight), Constants.WORLD_HEIGHT - halfHeight)));
    }
  }
}
