package it.unibo.donkeykong.ecs.system.api;

import it.unibo.donkeykong.core.api.World;

/**
 * This interface represents a system in the ECS (Entity-Component-System) architecture. A System is
 * responsible for processing entities that possess specific components, applying game logic, and
 * updating the state of those entities accordingly.
 */
public interface GameSystem {
  /**
   * Updates the system, processing all relevant entities.
   *
   * @param world the game world containing entities and components
   * @param deltaTime the time elapsed since the last update, in seconds
   */
  void update(World world, float deltaTime);
}
