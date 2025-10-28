package it.unibo.donkeykong.ecs.factory;

import it.unibo.donkeykong.ecs.component.Position;
import it.unibo.donkeykong.ecs.component.StateComponent.Direction;
import it.unibo.donkeykong.ecs.entity.Entity;

/** Factory interface for creating the main entities of the Donkey Kong game. */
public interface EntityFactory {
  /**
   * Creates the first player entity.
   *
   * @return a new entity representing the first player
   */
  Entity createFirstPlayer();

  /**
   * Creates the second player entity.
   *
   * @return a new entity representing the second player
   */
  Entity createSecondPlayer();

  /**
   * Creates the Pauline entity.
   *
   * @return a new entity representing Pauline
   */
  Entity createPauline();

  /**
   * Creates the Donkey Kong entity.
   *
   * @return a new entity representing Donkey Kong
   */
  Entity createDonkeyKong();

  /**
   * Creates a barrel entity.
   *
   * @param position the spawn position of the barrel
   * @param direction the initial direction of the barrel
   * @return a new entity representing a barrel
   */
  Entity createBarrel(Position position, Direction direction);

  /**
   * Creates a platform entity.
   *
   * @param position the position of the platform
   * @return a new entity representing a platform
   */
  Entity createPlatform(Position position);

  /**
   * Creates a ladder entity.
   *
   * @param position the position of the ladder
   * @return a new entity representing a ladder
   */
  Entity createLadder(Position position);
}
