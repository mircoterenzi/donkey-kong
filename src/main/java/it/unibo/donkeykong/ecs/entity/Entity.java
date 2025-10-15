package it.unibo.donkeykong.ecs.entity;

import it.unibo.donkeykong.ecs.component.Component;
import java.util.Optional;
import java.util.Set;

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
   * Gets the set of {@link Component} associated with this entity.
   *
   * @return a set of components
   */
  Set<Component> getComponents();

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
}
