package it.unibo.donkeykong.ecs.component;

import it.unibo.donkeykong.ecs.component.api.Component;

/** Gravity component, which represents the gravitational force affecting an entity. */
public record Gravity(double gravity) implements Component {}
