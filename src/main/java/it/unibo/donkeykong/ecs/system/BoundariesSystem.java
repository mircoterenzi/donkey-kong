package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.core.Constants.BOTTOM_THRESHOLD;

import it.unibo.donkeykong.core.Constants;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.api.Collider;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import java.util.ArrayList;
import java.util.List;

public class BoundariesSystem implements GameSystem {

  @Override
  public void update(World world, float deltaTime) {
    final List<Entity> entitiesToRemove = new ArrayList<>();

    for (Entity entity :
        world.getEntitiesWithComponents(List.of(PositionComponent.class, Collider.class))) {
      final PositionComponent position = entity.getComponent(PositionComponent.class).orElseThrow();
      final Collider collider = entity.getComponent(Collider.class).orElseThrow();
      final double halfWidth = collider.width() / 2.0;
      final double halfHeight = collider.height() / 2.0;
      final double newX =
          Math.min(Math.max(position.x(), halfWidth), Constants.WORLD_WIDTH - halfWidth);
      final double newY =
          Math.min(Math.max(position.y(), halfHeight), Constants.WORLD_HEIGHT - halfHeight);

      if (newX != position.x() || newY != position.y()) {
        if (entity.getComponent(BouncinessComponent.class).isPresent()
            && position.y() > BOTTOM_THRESHOLD
            && newX != position.x()) {
          entitiesToRemove.add(entity);
        } else {
          entity.updateComponent(position, new PositionComponent(newX, newY));
          if (entity.getComponent(BouncinessComponent.class).isPresent()) {
            entity
                .getComponent(VelocityComponent.class)
                .ifPresent(
                    velocity ->
                        entity.updateComponent(
                            velocity,
                            new VelocityComponent(
                                (newX != position.x() ? -1 : 1) * velocity.dx(),
                                (newY != position.y() ? -1 : 1) * velocity.dy())));
          }
        }
      }
    }

    for (final Entity e : entitiesToRemove) {
      world.removeEntity(e);
    }
  }
}
