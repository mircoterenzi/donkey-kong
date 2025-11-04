package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.core.Constants.LEFT_BARREL_SPAWN;
import static it.unibo.donkeykong.core.Constants.RIGHT_BARREL_SPAWN;
import static it.unibo.donkeykong.core.Constants.SPAWN_INTERVAL;
import static org.junit.jupiter.api.Assertions.*;

import it.unibo.donkeykong.core.WorldImpl;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.Bounciness;
import it.unibo.donkeykong.ecs.component.StateComponent.*;
import it.unibo.donkeykong.ecs.entity.EntityFactoryImpl;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SpawnSystemTest {
  private World world;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    SpawnSystem spawnSystem = new SpawnSystem(new EntityFactoryImpl(world));
    world.addSystem(spawnSystem);
  }

  @Test
  void testNoSpawnBeforeInterval() {
    assertTrue(
        world.getEntitiesWithComponents(List.of(Velocity.class, Bounciness.class)).isEmpty());
    world.update((SPAWN_INTERVAL - 1000f) / 1000f);
    assertTrue(
        world.getEntitiesWithComponents(List.of(Velocity.class, Bounciness.class)).isEmpty());
  }

  @Test
  void testSpawnOnInterval() {
    assertTrue(world.getEntitiesWithComponents(List.of(Bounciness.class)).isEmpty());
    Entity barrel = spawnBarrel();
    assertNotNull(barrel);
    assertEquals(1, world.getEntitiesWithComponents(List.of(Bounciness.class)).size());
  }

  @Test
  void testAlternatingSpawns() {
    Entity firstBarrel = spawnBarrel();
    assertEquals(
        Direction.LEFT, firstBarrel.getComponent(StateComponent.class).orElseThrow().direction());
    assertEquals(LEFT_BARREL_SPAWN, firstBarrel.getComponent(Position.class).orElseThrow());
    world.removeEntity(firstBarrel);
    Entity secondBarrel = spawnBarrel();
    assertEquals(
        Direction.RIGHT, secondBarrel.getComponent(StateComponent.class).orElseThrow().direction());
    assertEquals(RIGHT_BARREL_SPAWN, secondBarrel.getComponent(Position.class).orElseThrow());
    world.removeEntity(secondBarrel);
    Entity thirdBarrel = spawnBarrel();
    assertEquals(
        Direction.LEFT, thirdBarrel.getComponent(StateComponent.class).orElseThrow().direction());
    assertEquals(LEFT_BARREL_SPAWN, thirdBarrel.getComponent(Position.class).orElseThrow());
  }

  private Entity spawnBarrel() {
    world.update(SPAWN_INTERVAL / 1000f);
    return world.getEntitiesWithComponents(List.of(Bounciness.class)).stream()
        .findFirst()
        .orElseThrow();
  }
}
