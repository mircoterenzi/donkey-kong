package it.unibo.donkeykong.ecs.component;

import it.unibo.donkeykong.ecs.component.api.Component;

/** GravityComponent component, which represents the gravitational force affecting an entity. */
public record GravityComponent(double gravity) implements Component {}
