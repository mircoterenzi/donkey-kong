package it.unibo.donkeykong.ecs.component;

import it.unibo.donkeykong.ecs.component.api.Component;

/**
 * Represents the state of the player in the game.
 *
 * @param state the current state of the player
 */
public record StateComponent(State state, Direction direction) implements Component {
  public enum State {
    IDLE,
    MOVING,
    JUMP,
    FALL,
    UP,
    DOWN,
    STOP_CLIMB
  }

  public enum Direction {
    LEFT,
    RIGHT
  }
}
