package it.unibo.donkeykong.ecs.entity;

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
   * @return a new entity representing a barrel
   */
  Entity createBarrel();

  /**
   * Creates a platform entity.
   *
   * @return a new entity representing a platform
   */
  Entity createPlatform();

  /**
   * Creates a ladder entity.
   *
   * @return a new entity representing a ladder
   */
  Entity createLadder();
}
