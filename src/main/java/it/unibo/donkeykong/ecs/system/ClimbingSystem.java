package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.List;

/** ClimbingSystem handles the climbing mechanics for entities in the game world. */
public class ClimbingSystem extends CollisionEventHandlerSystem {

  @Override
  public void update(World world, long deltaTime) {
    super.handleCollision(
        world,
        List.of(Position.class, CollisionEvent.class),
        (entity) -> {
          Entity otherEntity =
              entity.getComponent(CollisionEvent.class).orElseThrow().otherEntity();
          if (otherEntity.getComponent(Climbable.class).isPresent()) {
            Position entityPos = entity.getComponent(Position.class).orElseThrow();
            Position otherPos = otherEntity.getComponent(Position.class).orElseThrow();
            if (entityPos.x() == otherPos.x()) {
              Gravity gravity = entity.getComponent(Gravity.class).orElseThrow();
              entity.removeComponent(gravity);
            }
          }
        });
  }
}
