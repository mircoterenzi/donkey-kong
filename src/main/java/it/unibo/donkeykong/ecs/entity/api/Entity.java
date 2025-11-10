package it.unibo.donkeykong.ecs.entity.api;

import it.unibo.donkeykong.ecs.component.api.Component;
import java.util.Optional;

/**
 * This interface represents an entity in the ECS (Entity-Component-System) architecture. An Entity
 * is a general-purpose object that can have multiple components attached to it, which define its
 * data and behavior.
 */
public interface Entity {
  /**
   * Gets the unique identifier of this entity.
   *
   * @return the entity ID
   */
  int getId();

  /**
   * Retrieves a {@link Component} of the specified class type if it exists.
   *
   * @param componentClass the class of the component to retrieve
   * @return an Optional containing the component if found, otherwise an empty Optional
   * @param <T> the type of the component
   */
  <T extends Component> Optional<T> getComponent(Class<T> componentClass);

  /**
   * Adds a {@link Component} to this entity.
   *
   * @param component the component to add
   * @return this entity
   */
  Entity addComponent(Component component);

  /**
   * Removes a {@link Component} from this entity.
   *
   * @param component the component to remove
   * @return this entity
   */
  Entity removeComponent(Component component);

  /**
   * Updates an existing {@link Component} with a new one.
   *
   * @param component the new component to add
   */
  void updateComponent(Component component);
}
