package it.unibo.donkeykong.ecs;

import it.unibo.donkeykong.ecs.component.Component;
import it.unibo.donkeykong.ecs.entity.Entity;
import it.unibo.donkeykong.ecs.system.GameSystem;
import java.util.*;
import java.util.stream.Collectors;

/** Implementation of the World interface. */
public class WorldImpl implements World {

  private int nextEntityId;
  private final Map<Entity, Set<Component>> componentsByEntity;
  private final List<GameSystem> systems;

  /** Implementation of the Entity interface. */
  private record EntityImpl(int id, World world) implements Entity {

    @Override
    public int getId() {
      return this.id;
    }

    @Override
    public Set<Component> getComponents() {
      return this.world.getComponentsOfEntity(this);
    }

    @Override
    public <T extends Component> Optional<T> getComponent(Class<T> componentClass) {
      return this.world.getComponentsOfEntity(this).stream()
          .filter(componentClass::isInstance)
          .map(componentClass::cast)
          .findFirst();
    }

    @Override
    public Entity addComponent(Component component) {
      this.world.addComponentToEntity(this, component);
      return this;
    }

    @Override
    public Entity removeComponent(Component component) {
      this.world.removeComponentFromEntity(this, component);
      return this;
    }
  }

  /** Constructor for WorldImpl. */
  public WorldImpl() {
    this.nextEntityId = 0;
    this.componentsByEntity = new HashMap<>();
    this.systems = new ArrayList<>();
  }

  @Override
  public Entity createEntity() {
    Entity entity = new EntityImpl(this.nextEntityId++, this);
    this.componentsByEntity.put(entity, new HashSet<>());
    return entity;
  }

  @Override
  public void addComponentToEntity(Entity entity, Component component) {
    this.componentsByEntity.get(entity).add(component);
  }

  @Override
  public Set<Component> getComponentsOfEntity(Entity entity) {
    return this.componentsByEntity.getOrDefault(entity, Set.of());
  }

  @Override
  public void removeComponentFromEntity(Entity entity, Component component) {
    this.componentsByEntity.get(entity).remove(component);
  }

  @Override
  public void removeEntity(Entity entity) {
    this.componentsByEntity.remove(entity);
  }

  @Override
  public Set<Entity> getEntitiesWithComponent(Class<? extends Component> componentClass) {
    return this.componentsByEntity.entrySet().stream()
        .filter(entry -> entry.getValue().stream().anyMatch(componentClass::isInstance))
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }

  @Override
  public void addSystem(GameSystem system) {
    this.systems.add(system);
  }

  @Override
  public void update(long deltaTime) {
    for (final var system : this.systems) {
      system.update(this, deltaTime);
    }
  }
}
