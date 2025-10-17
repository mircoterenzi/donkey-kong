package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.List;
import java.util.Set;

/** CollisionSystem handles collision detection and response between entities in the game world. */
public class CollisionSystem implements GameSystem {

  private static double clampOnRange(double value, double min, double max) {
    return Math.max(min, Math.min(value, max));
  }

  /* Credits to "2D Game CollisionEvent Detection" by Thomas Schwarzl */
  private static Position clampOnRectangle(
      Position position, Position rectanglePosition, RectangleCollider rectangleCollider) {
    double halfWidth = rectangleCollider.width() / 2.0;
    double halfHeight = rectangleCollider.height() / 2.0;
    return new Position(
        clampOnRange(
            position.x(), rectanglePosition.x() - halfWidth, rectanglePosition.x() + halfWidth),
        clampOnRange(
            position.y(), rectanglePosition.y() - halfHeight, rectanglePosition.y() + halfHeight));
  }

  private static boolean checkCollisionRectangleCircle(
      Position rectanglePosition,
      RectangleCollider rectangleCollider,
      Position circlePosition,
      CircleCollider circleCollider) {
    return checkCollisionCircleCircle(
        circlePosition,
        circleCollider,
        clampOnRectangle(circlePosition, rectanglePosition, rectangleCollider),
        new CircleCollider(0));
  }

  private static boolean checkCollisionCircleCircle(
      Position position,
      CircleCollider collider,
      Position otherPosition,
      CircleCollider otherCollider) {
    return position.distanceFrom(otherPosition) <= (collider.radius() + otherCollider.radius());
  }

  private static boolean checkCollisionRectangleRectangle(
      Position position,
      RectangleCollider collider,
      Position otherPosition,
      RectangleCollider otherCollider) {
    return Math.abs(position.x() - otherPosition.x())
            <= (collider.width() + otherCollider.width()) / 2.0
        && Math.abs(position.y() - otherPosition.y())
            <= (collider.height() + otherCollider.height()) / 2.0;
  }

  private boolean isColliding(
      Position position, Collider collider, Position otherPosition, Collider otherCollider) {
    if (collider instanceof RectangleCollider && otherCollider instanceof RectangleCollider) {
      return checkCollisionRectangleRectangle(
          position, (RectangleCollider) collider, otherPosition, (RectangleCollider) otherCollider);
    } else if (collider instanceof RectangleCollider && otherCollider instanceof CircleCollider) {
      return checkCollisionRectangleCircle(
          position, (RectangleCollider) collider, otherPosition, (CircleCollider) otherCollider);
    } else if (collider instanceof CircleCollider && otherCollider instanceof RectangleCollider) {
      return checkCollisionRectangleCircle(
          otherPosition, (RectangleCollider) otherCollider, position, (CircleCollider) collider);
    } else if (collider instanceof CircleCollider && otherCollider instanceof CircleCollider) {
      return checkCollisionCircleCircle(
          position, (CircleCollider) collider, otherPosition, (CircleCollider) otherCollider);
    } else {
      throw new IllegalArgumentException("One or more unknown collider types");
    }
  }

  @Override
  public void update(World world, long deltaTime) {
    Set<Entity> movingSolidEntities =
        world.getEntitiesWithComponents(List.of(Position.class, Collider.class, Velocity.class));
    Set<Entity> solidEntities =
        world.getEntitiesWithComponents(List.of(Position.class, Collider.class));

    for (Entity entity : movingSolidEntities) {
      Position position = entity.getComponent(Position.class).orElseThrow();
      Collider collider = entity.getComponent(Collider.class).orElseThrow();
      solidEntities.stream()
          .filter(otherEntity -> !otherEntity.equals(entity))
          .filter(
              otherEntity -> {
                Position otherPosition = otherEntity.getComponent(Position.class).orElseThrow();
                Collider otherCollider = otherEntity.getComponent(Collider.class).orElseThrow();
                return isColliding(position, collider, otherPosition, otherCollider);
              })
          .forEach(otherEntity -> entity.addComponent(new CollisionEvent(otherEntity)));
    }
  }
}
