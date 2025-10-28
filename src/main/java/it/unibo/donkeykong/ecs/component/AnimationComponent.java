package it.unibo.donkeykong.ecs.component;

public record AnimationComponent(
    int frameIndex, StateComponent.State state, float timeSinceLastFrame) implements Component {}
