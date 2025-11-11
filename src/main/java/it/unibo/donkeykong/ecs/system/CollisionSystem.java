package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.ecs.system.common.CollisionUtils.isColliding;

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
