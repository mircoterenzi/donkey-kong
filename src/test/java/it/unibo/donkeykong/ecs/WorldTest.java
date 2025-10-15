package it.unibo.donkeykong.ecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.donkeykong.ecs.component.Component;
import it.unibo.donkeykong.ecs.entity.Entity;
import it.unibo.donkeykong.ecs.systems.System;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorldTest {

  private World world;

  private static final class TestComponent implements Component {}

  private static final class AnotherComponent implements Component {}

  /** A simple test system that records the last delta time and world it was updated with. */
  private static class TestSystem implements System {
    private long lastDeltaTime = -1;
    private World lastWorld = null;
    private int updateCount = 0;

    @Override
    public void update(World world, long deltaTime) {
      this.lastWorld = world;
      this.lastDeltaTime = deltaTime;
      this.updateCount++;
    }

    public long getLastDeltaTime() {
      return lastDeltaTime;
    }

    public World getLastWorld() {
      return lastWorld;
    }

    public int getUpdateCount() {
      return updateCount;
    }

    public Set<Entity> getEntitiesToProcess(World currentWorld) {
      return currentWorld.getEntitiesWithComponent(TestComponent.class);
    }
  }

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
  }

  @Test
  void testCreateEntity() {
    Entity entity = world.createEntity();
    assertNotNull(entity);
  }

  @Test
  void testRemoveEntity() {
    Entity entity = world.createEntity();
    TestComponent comp = new TestComponent();
    world.addComponentToEntity(entity, comp);

    Set<Entity> entitiesWithComp = world.getEntitiesWithComponent(TestComponent.class);
    assertTrue(entitiesWithComp.contains(entity));

    world.removeEntity(entity);

    entitiesWithComp = world.getEntitiesWithComponent(TestComponent.class);
    assertFalse(entitiesWithComp.contains(entity));
    assertTrue(world.getComponentsOfEntity(entity).isEmpty());
  }

  @Test
  void testAddComponentToEntity() {
    Entity entity = world.createEntity();
    TestComponent comp = new TestComponent();

    world.addComponentToEntity(entity, comp);
    Set<Component> components = world.getComponentsOfEntity(entity);

    assertTrue(components.contains(comp));
    assertEquals(1, components.size());
  }

  @Test
  void testGetComponentsOfEntity() {
    Entity entity = world.createEntity();
    TestComponent comp1 = new TestComponent();
    AnotherComponent comp2 = new AnotherComponent();
    world.addComponentToEntity(entity, comp1);
    world.addComponentToEntity(entity, comp2);

    Set<Component> components = world.getComponentsOfEntity(entity);
    assertEquals(2, components.size());
    assertTrue(components.contains(comp1));
    assertTrue(components.contains(comp2));
  }

  @Test
  void testRemoveComponentFromEntity() {
    Entity entity = world.createEntity();
    TestComponent compToKeep = new TestComponent();
    AnotherComponent compToRemove = new AnotherComponent();
    world.addComponentToEntity(entity, compToKeep);
    world.addComponentToEntity(entity, compToRemove);

    world.removeComponentFromEntity(entity, compToRemove);

    Set<Component> componentsAfter = world.getComponentsOfEntity(entity);
    assertFalse(componentsAfter.contains(compToRemove));
    assertTrue(componentsAfter.contains(compToKeep));
    assertEquals(1, componentsAfter.size());
  }

  @Test
  void testGetEntitiesWithComponent() {
    Entity e1 = world.createEntity();
    Entity e2 = world.createEntity();
    Entity e3 = world.createEntity();

    world.addComponentToEntity(e1, new TestComponent());
    world.addComponentToEntity(e1, new AnotherComponent());
    world.addComponentToEntity(e2, new TestComponent());
    world.addComponentToEntity(e3, new AnotherComponent());

    Set<Entity> entitiesWithTest = world.getEntitiesWithComponent(TestComponent.class);
    assertEquals(2, entitiesWithTest.size());
    assertTrue(entitiesWithTest.contains(e1));
    assertTrue(entitiesWithTest.contains(e2));
    assertFalse(entitiesWithTest.contains(e3));

    Set<Entity> entitiesWithAnother = world.getEntitiesWithComponent(AnotherComponent.class);
    assertEquals(2, entitiesWithAnother.size());
    assertTrue(entitiesWithAnother.contains(e1));
    assertTrue(entitiesWithAnother.contains(e3));
    assertFalse(entitiesWithAnother.contains(e2));
  }

  @Test
  void testAddSystemAndWorldUpdate() {
    TestSystem testSystem = new TestSystem();
    world.addSystem(testSystem);

    long deltaTime = 33L;
    world.update(deltaTime);

    assertEquals(deltaTime, testSystem.getLastDeltaTime());
    assertEquals(world, testSystem.getLastWorld());
    assertEquals(1, testSystem.getUpdateCount());
  }

  @Test
  void testSystemCanQueryCorrectEntitiesAfterUpdate() {
    TestSystem testSystem = new TestSystem();
    world.addSystem(testSystem);

    Entity entity1 = world.createEntity();
    world.addComponentToEntity(entity1, new TestComponent());
    Entity entity2 = world.createEntity();
    world.addComponentToEntity(entity2, new AnotherComponent());

    world.update(10L);

    World updatedWorld = testSystem.getLastWorld();
    assertNotNull(updatedWorld);

    Set<Entity> processedBySystem = testSystem.getEntitiesToProcess(updatedWorld);

    assertEquals(1, processedBySystem.size());
    assertTrue(processedBySystem.contains(entity1));
    assertFalse(processedBySystem.contains(entity2));
  }
}
