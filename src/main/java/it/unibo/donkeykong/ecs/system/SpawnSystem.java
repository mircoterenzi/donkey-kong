package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.utilities.Constants.*;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.StateComponent.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import it.unibo.donkeykong.ecs.factory.EntityFactory;

public class SpawnSystem implements GameSystem {
  private final EntityFactory entityFactory;
  private float elapsedTime;
  private boolean left;

  public SpawnSystem(EntityFactory entityFactory) {
    this.entityFactory = entityFactory;
    this.elapsedTime = 0;
    this.left = true;
  }

  @Override
  public void update(World world, float deltaTime) {
    this.elapsedTime += deltaTime * 1000;
    if (this.elapsedTime >= SPAWN_INTERVAL) {
      Entity entity =
          this.left
              ? this.entityFactory.createBarrel(LEFT_BARREL_SPAWN, Direction.LEFT)
              : this.entityFactory.createBarrel(RIGHT_BARREL_SPAWN, Direction.RIGHT);
      left = !left;
      elapsedTime = 0;
    }
  }
}
