package it.unibo.donkeykong.ecs.component;

/**
 * Represents the state of the player in the game.
 *
 * @param state the current state of the player
 */
public record PlayerState(State state) implements Component {
  public enum State {
    STOP_GROUND,
    RUN_LEFT,
    RUN_RIGHT,
    JUMP,
    FALL,
    CLIMB_UP,
    CLIMB_DOWN,
    STOP_CLIMB
  }
}
