package it.unibo.donkeykong.ecs.component;

/**
 * Position component, which represents the position of an entity.
 *
 * @param x the x-coordinate
 * @param y the y-coordinate
 */
public record Position(double x, double y) implements Component {}
