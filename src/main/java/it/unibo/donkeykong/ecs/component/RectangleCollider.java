package it.unibo.donkeykong.ecs.component;

import it.unibo.donkeykong.ecs.component.api.Collider;

public record RectangleCollider(int width, int height) implements Collider {}
