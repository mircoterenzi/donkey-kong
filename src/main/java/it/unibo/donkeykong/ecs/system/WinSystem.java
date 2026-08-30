package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.CollisionEventComponent;
import it.unibo.donkeykong.ecs.component.GoalComponent;
import it.unibo.donkeykong.ecs.component.InputComponent;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import java.util.List;
import java.util.function.Consumer;

public class WinSystem implements GameSystem {
  private final Consumer<Entity> onWinCallback;

  public WinSystem(final Consumer<Entity> onWinCallback) {
    this.onWinCallback = onWinCallback;
  }

  @Override
  public void update(World world, float deltaTime) {
    world
        .getEntitiesWithComponents(List.of(CollisionEventComponent.class, InputComponent.class))
        .forEach(
            entity -> {
              CollisionEventComponent collision =
                  entity.getComponent(CollisionEventComponent.class).orElseThrow();
              if (collision.hasCollisionsWith(GoalComponent.class)) {
                onWinCallback.accept(entity);
              }
            });
  }
}
