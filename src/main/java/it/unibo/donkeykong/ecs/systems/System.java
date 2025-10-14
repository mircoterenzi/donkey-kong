package it.unibo.donkeykong.ecs.systems;

/**
 * This interface represents a system in the ECS (Entity-Component-System) architecture. A System is
 * responsible for processing entities that possess specific components, applying game logic, and
 * updating the state of those entities accordingly.
 */
public interface System {
  /**
   * Updates the system, processing all relevant entities.
   *
   * @param deltaTime the time elapsed since the last update, in milliseconds
   */
  void update(long deltaTime);
}
