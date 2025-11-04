package it.unibo.donkeykong.ecs.component;

import it.unibo.donkeykong.ecs.component.api.Component;

public record AnimationComponent(
    int frameIndex, StateComponent.State state, float timeSinceLastFrame) implements Component {}
