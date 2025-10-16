package it.unibo.donkeykong.ecs.component;

import static it.unibo.donkeykong.utilities.Constants.GRAVITY;

/** Gravity component, which represents the gravitational force affecting an entity. */
public class Gravity implements Component {
  private final double gravity;

  public Gravity() {
    this.gravity = GRAVITY;
  }

  public double getGravity() {
    return gravity;
  }
}
