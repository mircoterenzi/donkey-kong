package it.unibo.donkeykong.ecs.component;

import it.unibo.donkeykong.ecs.component.api.Component;

public record NetworkComponent(String networkId, String entityType) implements Component {}
