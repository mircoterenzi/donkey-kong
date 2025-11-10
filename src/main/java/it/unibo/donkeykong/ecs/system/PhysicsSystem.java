package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.api.Collider;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import java.util.List;
import java.util.Set;

/**
 * PhysicsSystem is responsible for handling physics-related updates in the game world, such as a
 * moving entity trying to pass through a static obstacle.
 */
public class PhysicsSystem implements GameSystem {

  private static void resetVerticalVelocity(Entity entity) {
    entity
        .getComponent(VelocityComponent.class)
        .ifPresent(velocity -> entity.updateComponent(new VelocityComponent(velocity.dx(), 0)));
  }

  private static void handleCollision(Entity entity, Entity otherEntity) {
    PositionComponent position = entity.getComponent(PositionComponent.class).orElseThrow();
    Collider collider = entity.getComponent(Collider.class).orElseThrow();
    PositionComponent otherPosition =
        otherEntity.getComponent(PositionComponent.class).orElseThrow();
    Collider otherCollider = otherEntity.getComponent(Collider.class).orElseThrow();
    double newX = position.x();
    double newY = position.y();
    double minDistanceX = (collider.width() + otherCollider.width()) / 2.0;
    double minDistanceY = (collider.height() + otherCollider.height()) / 2.0;
    double actualDistanceX = position.x() - otherPosition.x();
    double actualDistanceY = position.y() - otherPosition.y();
    double overlapX = minDistanceX - Math.abs(actualDistanceX);
    double overlapY = minDistanceY - Math.abs(actualDistanceY);

    if (overlapX > 0 && overlapY > 0) {
      if (overlapX < overlapY) {
        newX = position.x() + (actualDistanceX > 0 ? overlapX : -overlapX);
      } else {
        newY = position.y() + (actualDistanceY > 0 ? overlapY : -overlapY);
      }
    }
    if (newX != position.x() || newY != position.y()) {
      entity.updateComponent(new PositionComponent(newX, newY));
      if (newY <= position.y()) {
        resetVerticalVelocity(entity);
      }
    }
  }

  @Override
  public void update(World world, float deltaTime) {
    Set<Entity> targetEntities =
        world.getEntitiesWithComponents(
            List.of(
                CollisionEventComponent.class,
                PositionComponent.class,
                VelocityComponent.class,
                Collider.class));
    targetEntities.forEach(
        entity ->
            entity
                .getComponent(CollisionEventComponent.class)
                .ifPresent(
                    collisionEvent ->
                        collisionEvent.getCollisionsWith(PositionComponent.class).stream()
                            .filter(
                                otherEntity ->
                                    otherEntity.getComponent(SolidComponent.class).isPresent())
                            .forEach(otherEntity -> handleCollision(entity, otherEntity))));
  }
}
