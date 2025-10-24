package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.List;
import java.util.Set;

/** ClimbingSystem handles the climbing mechanics for entities in the game world. */
public class ClimbingSystem implements GameSystem {

  @Override
  public void update(World world, long deltaTime) {
    Set<Entity> targetEntities =
        world.getEntitiesWithComponents(
            List.of(Position.class, CollisionEvent.class, CollisionEvent.class));
    for (Entity entity : targetEntities) {
      CollisionEvent event = entity.getComponent(CollisionEvent.class).orElseThrow();
      Entity otherEntity = event.otherEntity();
      if (otherEntity.getComponent(Climbable.class).isPresent()) {
        Position entityPos = entity.getComponent(Position.class).orElseThrow();
        Position otherPos = otherEntity.getComponent(Position.class).orElseThrow();
        if (entityPos.x() == otherPos.x()) {
          entity.getComponent(Gravity.class).ifPresent(entity::removeComponent);
        }
      }
    }
  }
}
