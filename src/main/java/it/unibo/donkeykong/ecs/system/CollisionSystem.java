package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.ecs.system.common.CollisionUtils.clampOnRectangle;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.api.Collider;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** CollisionSystem handles collision detection between entities in the game world. */
public class CollisionSystem implements GameSystem {

  private static boolean checkCollisionRectangleCircle(
      PositionComponent rectanglePosition,
      RectangleCollider rectangleCollider,
      PositionComponent circlePosition,
      CircleCollider circleCollider) {
    return checkCollisionCircleCircle(
        circlePosition,
        circleCollider,
        clampOnRectangle(circlePosition, rectanglePosition, rectangleCollider),
        new CircleCollider(0));
  }

  private static boolean checkCollisionCircleCircle(
      PositionComponent position,
      CircleCollider collider,
      PositionComponent otherPosition,
      CircleCollider otherCollider) {
    return position.distanceFrom(otherPosition) <= (collider.radius() + otherCollider.radius());
  }

  private static boolean checkCollisionRectangleRectangle(
      PositionComponent position,
      RectangleCollider collider,
      PositionComponent otherPosition,
      RectangleCollider otherCollider) {
    return Math.abs(position.x() - otherPosition.x())
            <= (collider.width() + otherCollider.width()) / 2.0
        && Math.abs(position.y() - otherPosition.y())
            <= (collider.height() + otherCollider.height()) / 2.0;
  }

  private boolean isColliding(
      PositionComponent position,
      Collider collider,
      PositionComponent otherPosition,
      Collider otherCollider) {
    if (collider instanceof RectangleCollider rectangleCollider
        && otherCollider instanceof RectangleCollider otherRectangleCollider) {
      return checkCollisionRectangleRectangle(
          position, rectangleCollider, otherPosition, otherRectangleCollider);
    } else if (collider instanceof RectangleCollider rectangleCollider
        && otherCollider instanceof CircleCollider circleCollider) {
      return checkCollisionRectangleCircle(
          position, rectangleCollider, otherPosition, circleCollider);
    } else if (collider instanceof CircleCollider circleCollider
        && otherCollider instanceof RectangleCollider rectangleCollider) {
      return checkCollisionRectangleCircle(
          otherPosition, rectangleCollider, position, circleCollider);
    } else if (collider instanceof CircleCollider circleCollider
        && otherCollider instanceof CircleCollider otherCircleCollider) {
      return checkCollisionCircleCircle(
          position, circleCollider, otherPosition, otherCircleCollider);
    } else {
      throw new IllegalArgumentException("One or more unknown collider types");
    }
  }

  @Override
  public void update(World world, float deltaTime) {
    Set<Entity> movingSolidEntities =
        world.getEntitiesWithComponents(
            List.of(PositionComponent.class, Collider.class, VelocityComponent.class));
    Set<Entity> solidEntities =
        world.getEntitiesWithComponents(List.of(PositionComponent.class, Collider.class));

    for (Entity entity : movingSolidEntities) {
      PositionComponent position = entity.getComponent(PositionComponent.class).orElseThrow();
      Collider collider = entity.getComponent(Collider.class).orElseThrow();
      solidEntities.stream()
          .filter(otherEntity -> !otherEntity.equals(entity))
          .filter(
              otherEntity -> {
                PositionComponent otherPosition =
                    otherEntity.getComponent(PositionComponent.class).orElseThrow();
                Collider otherCollider = otherEntity.getComponent(Collider.class).orElseThrow();
                return isColliding(position, collider, otherPosition, otherCollider);
              })
          .forEach(
              otherEntity -> {
                Optional<CollisionEventComponent> event =
                    entity.getComponent(CollisionEventComponent.class);
                if (event.isPresent()) {
                  event.get().addCollision(otherEntity);
                } else {
                  entity.addComponent(new CollisionEventComponent(otherEntity));
                }
              });
    }
  }
}
