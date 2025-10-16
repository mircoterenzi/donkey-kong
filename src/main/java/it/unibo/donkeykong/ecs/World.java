package it.unibo.donkeykong.ecs;

import it.unibo.donkeykong.ecs.component.Component;
import it.unibo.donkeykong.ecs.entity.Entity;
import it.unibo.donkeykong.ecs.system.GameSystem;
import java.util.List;
import java.util.Set;

/**
 * This interface represents the game world, which is responsible for managing entities and their
 * interactions.
 */
public interface World {

  /**
   * Creates a new {@link Entity} in the world.
   *
   * @return the newly created entity
   */
  Entity createEntity();

  /**
   * Adds a {@link Component} to the specified {@link Entity}.
   *
   * @param entity the entity to which the component will be added
   * @param component the component to add
   */
  void addComponentToEntity(Entity entity, Component component);

  /**
   * Retrieves all {@link Component}s associated with the specified {@link Entity}.
   *
   * @param entity the entity whose components are to be retrieved
   * @return a set of components associated with the entity
   */
  Set<Component> getComponentsOfEntity(Entity entity);

  /**
   * Removes a {@link Component} from the specified {@link Entity}.
   *
   * @param entity the entity from which the component will be removed
   * @param component the component to remove
   */
  void removeComponentFromEntity(Entity entity, Component component);

  /**
   * Removes the specified {@link Entity} from the world.
   *
   * @param entity the entity to remove
   */
  void removeEntity(Entity entity);

  /**
   * Retrieves all entities that have the specified component class.
   *
   * @param componentClass the class of the component to filter entities by
   * @return a set of entities that have the specified component
   */
  Set<Entity> getEntitiesWithComponents(List<Class<? extends Component>> componentClass);

  /**
   * Adds a {@link GameSystem} to the world, which will be updated during the world's update cycle.
   *
   * @param system the system to add
   */
  void addSystem(GameSystem system);

  /**
   * Updates the world state, including all entities and their components.
   *
   * @param deltaTime the time elapsed since the last update, in milliseconds
   */
  void update(long deltaTime);
}
