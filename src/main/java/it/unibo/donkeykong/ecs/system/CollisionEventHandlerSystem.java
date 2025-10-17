package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.CollisionEvent;
import it.unibo.donkeykong.ecs.component.Component;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/** CollisionEventHandlerSystem provides a template method to handle collision events. */
public abstract class CollisionEventHandlerSystem implements GameSystem {

  /**
   * Handles collision events for entities with the specified components by applying the given
   * collision logic, then removes the {@link CollisionEvent} components from both involved
   * entities.
   *
   * @param world the game world
   * @param componentsRequired the list of components required for the entities to be processed
   * @param collisionLogic the logic to apply on collision
   */
  protected void handleCollision(
      World world,
      List<Class<? extends Component>> componentsRequired,
      Consumer<Entity> collisionLogic) {
    Set<Entity> targetEntities = world.getEntitiesWithComponents(componentsRequired);
    for (Entity entity : targetEntities) {
      CollisionEvent event = entity.getComponent(CollisionEvent.class).orElseThrow();
      collisionLogic.accept(entity);
      world.removeComponentFromEntity(entity, event);
      world.removeComponentFromEntity(event.otherEntity(), event);
    }
  }
}
