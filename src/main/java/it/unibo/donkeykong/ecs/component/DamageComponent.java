package it.unibo.donkeykong.ecs.component;

import it.unibo.donkeykong.ecs.component.api.Component;

public record DamageComponent(int damageAmount) implements Component {}
