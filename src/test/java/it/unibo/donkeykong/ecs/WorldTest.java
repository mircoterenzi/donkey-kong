package it.unibo.donkeykong.ecs;

import static org.junit.jupiter.api.Assertions.*;

import it.unibo.donkeykong.ecs.component.Component;
import it.unibo.donkeykong.ecs.entity.Entity;
import it.unibo.donkeykong.ecs.system.GameSystem;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorldTest {

  private static final class TestComponent implements Component {}

  private static final class AnotherComponent implements Component {}

  private static final long DELTA_TIME = 20L;
  private static final int UPDATES = 100;

  private World world;

  /** A simple test system that records the last delta time and world it was updated with. */
  private static class TestSystem implements GameSystem {
    private float deltaTime = -1;
    private World world = null;
    private int updateCount = 0;

    @Override
    public void update(World world, float deltaTime) {
      this.world = world;
      this.deltaTime = deltaTime;
      this.updateCount++;
    }

    public float getDeltaTime() {
      return deltaTime;
    }

    public World getWorld() {
      return world;
    }

    public int getUpdateCount() {
      return updateCount;
    }

    public Set<Entity> getEntitiesToProcess(World currentWorld) {
      return currentWorld.getEntitiesWithComponents(List.of(TestComponent.class));
    }
  }

  @BeforeEach
  void setUp() {
    this.world = new WorldImpl();
  }

  @Test
  void testCreateEntity() {
    Entity entity = this.world.createEntity();
    assertNotNull(entity);
  }

  @Test
  void testAddComponentToEntity() {
    TestComponent component = new TestComponent();
    Entity entity = world.createEntity().addComponent(component);
    Set<Entity> entitiesWithComponent =
        world.getEntitiesWithComponents(List.of(TestComponent.class));
    assertTrue(entitiesWithComponent.contains(entity));
    assertEquals(Set.of(component), world.getComponentsOfEntity(entity));
  }

  @Test
  void testRemoveEntity() {
    Entity entity = world.createEntity().addComponent(new TestComponent());
    world.removeEntity(entity);
    Set<Entity> entitiesWithComponent =
        world.getEntitiesWithComponents(List.of(TestComponent.class));
    assertFalse(entitiesWithComponent.contains(entity));
    assertTrue(world.getComponentsOfEntity(entity).isEmpty());
  }

  @Test
  void testGetComponentsOfEntity() {
    TestComponent component = new TestComponent();
    AnotherComponent anotherComponent = new AnotherComponent();
    Entity entity = world.createEntity().addComponent(component).addComponent(anotherComponent);
    Set<Component> components = world.getComponentsOfEntity(entity);
    assertEquals(2, components.size());
    assertTrue(components.contains(component));
    assertTrue(components.contains(anotherComponent));
  }

  @Test
  void testRemoveComponentFromEntity() {
    TestComponent componentToKeep = new TestComponent();
    AnotherComponent componentToRemove = new AnotherComponent();
    TestComponent anotherComponentToRemove = new TestComponent();
    Entity entity =
        world
            .createEntity()
            .addComponent(componentToKeep)
            .addComponent(componentToRemove)
            .addComponent(anotherComponentToRemove);
    entity.removeComponent(componentToRemove).removeComponent(anotherComponentToRemove);
    Set<Component> componentsAfter = world.getComponentsOfEntity(entity);
    assertFalse(componentsAfter.contains(componentToRemove));
    assertFalse(componentsAfter.contains(anotherComponentToRemove));
    assertEquals(Set.of(componentToKeep), componentsAfter);
  }

  @Test
  void testUpdateComponent() {
    Component oldComponent = new TestComponent();
    Component newComponent = new TestComponent();
    Entity entity = world.createEntity().addComponent(oldComponent);

    assertTrue(
        entity.getComponent(TestComponent.class).isPresent(), "Old component should be present.");
    assertSame(
        oldComponent,
        entity.getComponent(TestComponent.class).get(),
        "Instance should be the old one.");

    entity.updateComponent(oldComponent, newComponent);

    Set<Component> components = world.getComponentsOfEntity(entity);
    assertFalse(components.contains(oldComponent), "Old component should be removed.");
    assertTrue(components.contains(newComponent), "New component should be added.");
    assertSame(
        newComponent,
        entity.getComponent(TestComponent.class).get(),
        "Instance should be the new one.");
    assertEquals(1, components.size(), "Should only be one component.");
  }

  @Test
  void testEntityIdsAreUnique() {
    Entity entity = world.createEntity();
    Entity anotherEntity = world.createEntity();
    assertNotEquals(entity, anotherEntity);
  }

  @Test
  void testEntityIdsAreReused() {
    Entity entity = world.createEntity();
    world.removeEntity(entity);
    Entity entityGeneratedAfter = world.createEntity();
    Entity anotherEntity = world.createEntity();
    assertEquals(entity.getId(), entityGeneratedAfter.getId());
    assertNotEquals(entityGeneratedAfter, anotherEntity);
  }

  @Test
  void testGetEntitiesWithComponent() {
    Entity e1 =
        world.createEntity().addComponent(new TestComponent()).addComponent(new AnotherComponent());
    Entity e2 = world.createEntity().addComponent(new TestComponent());
    Entity e3 = world.createEntity().addComponent(new AnotherComponent());
    Set<Entity> entitiesWithTestComponent =
        world.getEntitiesWithComponents(List.of(TestComponent.class));
    Set<Entity> entitiesWithAnotherComponent =
        world.getEntitiesWithComponents(List.of(AnotherComponent.class));
    assertEquals(Set.of(e1, e2), entitiesWithTestComponent);
    assertEquals(Set.of(e1, e3), entitiesWithAnotherComponent);
  }

  @Test
  void testAddSystemAndWorldUpdate() {
    TestSystem testSystem = new TestSystem();
    world.addSystem(testSystem);
    for (int count = 0; count < UPDATES; count++) {
      world.update(DELTA_TIME);
    }
    assertEquals(DELTA_TIME, testSystem.getDeltaTime());
    assertEquals(world, testSystem.getWorld());
    assertEquals(UPDATES, testSystem.getUpdateCount());
  }

  @Test
  void testSystemCanQueryCorrectEntitiesAfterUpdate() {
    TestSystem testSystem = new TestSystem();
    world.addSystem(testSystem);
    Component testComponent = new TestComponent();
    Entity entity = world.createEntity().addComponent(testComponent);
    Entity anotherEntity = world.createEntity().addComponent(new AnotherComponent());
    world.update(DELTA_TIME);
    World updatedWorld = testSystem.getWorld();
    Set<Entity> processedBySystem = testSystem.getEntitiesToProcess(updatedWorld);
    assertNotNull(updatedWorld);
    assertTrue(processedBySystem.contains(entity));
    assertFalse(processedBySystem.contains(anotherEntity));
  }
}
