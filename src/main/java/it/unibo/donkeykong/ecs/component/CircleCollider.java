package it.unibo.donkeykong.ecs.component;

public record CircleCollider(int radius) implements Collider {
  @Override
  public int width() {
    return this.radius * 2;
  }

  @Override
  public int height() {
    return this.radius * 2;
  }
}
