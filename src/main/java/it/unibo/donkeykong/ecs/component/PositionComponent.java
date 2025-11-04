package it.unibo.donkeykong.ecs.component;

import it.unibo.donkeykong.ecs.component.api.Component;

/**
 * PositionComponent component, which represents the position of an entity.
 *
 * @param x the x-coordinate
 * @param y the y-coordinate
 */
public record PositionComponent(double x, double y) implements Component {

  /**
   * Calculates the distance from another {@link PositionComponent}.
   *
   * @param other the other position
   * @return the distance between this position and the other position
   */
  public double distanceFrom(PositionComponent other) {
    return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
  }
}
