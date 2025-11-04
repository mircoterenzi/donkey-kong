package it.unibo.donkeykong.ecs.component;

import it.unibo.donkeykong.ecs.component.api.Component;

/**
 * VelocityComponent component, which represents the speed and direction of an entity.
 *
 * @param dx the change in x-coordinate
 * @param dy the change in y-coordinate
 */
public record VelocityComponent(double dx, double dy) implements Component {}
