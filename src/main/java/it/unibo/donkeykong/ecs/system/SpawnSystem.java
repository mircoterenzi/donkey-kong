package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.core.Constants.*;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.StateComponent.*;
import it.unibo.donkeykong.ecs.entity.api.EntityFactory;
import it.unibo.donkeykong.ecs.system.api.GameSystem;

public class SpawnSystem implements GameSystem {
  private final EntityFactory entityFactory;
  private float elapsedTime;
  private double velocity;

  public SpawnSystem(EntityFactory entityFactory) {
    this.entityFactory = entityFactory;
    this.elapsedTime = 0;
    this.velocity = -BARREL_VELOCITY;
  }

  @Override
  public void update(World world, float deltaTime) {
    this.elapsedTime += deltaTime * 1000;
    if (this.elapsedTime >= SPAWN_INTERVAL) {
      entityFactory.createBarrel(velocity);
      velocity = -velocity;
      elapsedTime = 0;
    }
  }
}
