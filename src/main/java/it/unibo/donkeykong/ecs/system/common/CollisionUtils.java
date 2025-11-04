package it.unibo.donkeykong.ecs.system.common;

import it.unibo.donkeykong.ecs.component.PositionComponent;
import it.unibo.donkeykong.ecs.component.RectangleCollider;

/**
 * Utility class for collision detection and handling. Credits to "2D Game Collision Detection" by
 * Thomas Schwarzl.
 */
public final class CollisionUtils {

  private CollisionUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Clamps a value within a specified range.
   *
   * @param value the value to clamp
   * @param min the minimum bound
   * @param max the maximum bound
   * @return the clamped value
   */
  public static double clampOnRange(double value, double min, double max) {
    return Math.max(min, Math.min(value, max));
  }

  /**
   * Clamps a position on the bounds of a rectangle defined by its position and collider.
   *
   * @param position the position to clamp
   * @param rectanglePosition the position of the rectangle
   * @param rectangleCollider the rectangle collider
   * @return the clamped position
   */
  public static PositionComponent clampOnRectangle(
      PositionComponent position,
      PositionComponent rectanglePosition,
      RectangleCollider rectangleCollider) {
    double halfWidth = rectangleCollider.width() / 2.0;
    double halfHeight = rectangleCollider.height() / 2.0;
    return new PositionComponent(
        clampOnRange(
            position.x(), rectanglePosition.x() - halfWidth, rectanglePosition.x() + halfWidth),
        clampOnRange(
            position.y(), rectanglePosition.y() - halfHeight, rectanglePosition.y() + halfHeight));
  }
}
