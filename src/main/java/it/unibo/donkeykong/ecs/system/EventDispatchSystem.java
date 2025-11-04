package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.api.EventComponent;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import java.util.List;

/**
 * EventDispatchSystem is responsible for clearing event components at the end of each update cycle.
 */
public class EventDispatchSystem implements GameSystem {

  @Override
  public void update(World world, float deltaTime) {
    world
        .getEntitiesWithComponents(List.of(EventComponent.class))
        .forEach(
            entity -> entity.getComponent(EventComponent.class).ifPresent(entity::removeComponent));
  }
}
