package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.List;
import java.util.Set;

/**
 * PhysicsSystem is responsible for handling physics-related updates in the game world, such as a
 * moving entity trying to pass through a static obstacle.
 */
public class PhysicsSystem implements GameSystem {

  private static void handleCollision(long deltaTime, Entity entity, Entity otherEntity) {
    Velocity velocity = entity.getComponent(Velocity.class).orElseThrow();
    Position position = entity.getComponent(Position.class).orElseThrow();
    Position otherPosition = otherEntity.getComponent(Position.class).orElseThrow();
    double overlapX = position.x() - otherPosition.x();
    double overlapY = position.y() - otherPosition.y();
    if (Math.abs(overlapY) < Math.abs(overlapX)) {
      entity.updateComponent(
          position, new Position(position.x() + (-velocity.dx() * deltaTime), position.y()));
    } else {
      entity.updateComponent(
          position, new Position(position.x(), position.y() + (-velocity.dy() * deltaTime)));
    }
    entity
        .getComponent(Bounciness.class)
        .ifPresent(
            bounciness ->
                entity.updateComponent(velocity, new Velocity(-velocity.dx(), -velocity.dy())));
  }

  @Override
  public void update(World world, long deltaTime) {
    Set<Entity> targetEntities =
        world.getEntitiesWithComponents(
            List.of(CollisionEvent.class, Position.class, Velocity.class, Collider.class));
    targetEntities.forEach(
        entity ->
            entity
                .getComponent(CollisionEvent.class)
                .ifPresent(
                    collisionEvent ->
                        collisionEvent
                            .getCollisionsWith(Position.class)
                            .forEach(
                                otherEntity -> handleCollision(deltaTime, entity, otherEntity))));
  }
}
