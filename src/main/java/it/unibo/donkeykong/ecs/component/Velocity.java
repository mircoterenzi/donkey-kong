package it.unibo.donkeykong.ecs.component;

/**
 * Velocity component, which represents the speed and direction of an entity.
 *
 * @param dx the change in x-coordinate
 * @param dy the change in y-coordinate
 */
public record Velocity(double dx, double dy) implements Component {}
