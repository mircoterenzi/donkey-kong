package it.unibo.donkeykong.ecs.component;

import it.unibo.donkeykong.ecs.entity.Entity;

public record CollisionEvent(Entity otherEntity) implements Component {}
