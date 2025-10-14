package it.unibo.donkeykong.ecs;

import it.unibo.donkeykong.ecs.component.Component;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.List;

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
   * Creates a new {@link Entity} in the world with the specified components.
   *
   * @param components the list of components to add to the entity
   * @return the newly created entity
   */
  Entity createEntity(List<Component> components);

  /**
   * Adds a {@link Component} to the specified {@link Entity}.
   *
   * @param entity the entity to which the component will be added
   * @param component the component to add
   */
  void addComponentToEntity(Entity entity, Component component);

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
   * Updates the world state, including all entities and their components.
   *
   * @param deltaTime the time elapsed since the last update, in milliseconds
   */
  void update(long deltaTime);
}
