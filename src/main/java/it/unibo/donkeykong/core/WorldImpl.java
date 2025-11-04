package it.unibo.donkeykong.core;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.api.Component;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import java.util.*;
import java.util.stream.Collectors;

/** Implementation of the World interface. */
public class WorldImpl implements World {

  private int nextEntityId;
  private final Queue<Integer> availableIds;
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

    @Override
    public Entity updateComponent(Component oldComponent, Component newComponent) {
      this.world.removeComponentFromEntity(this, oldComponent);
      this.world.addComponentToEntity(this, newComponent);
      return this;
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Entity entity && entity.getId() == this.id;
    }

    @Override
    public int hashCode() {
      return Long.hashCode(id);
    }
  }

  /** Constructor for WorldImpl. */
  public WorldImpl() {
    this.nextEntityId = 0;
    this.availableIds = new LinkedList<>();
    this.componentsByEntity = new HashMap<>();
    this.systems = new ArrayList<>();
  }

  @Override
  public Entity createEntity() {
    Entity entity =
        new EntityImpl(availableIds.isEmpty() ? nextEntityId++ : availableIds.poll(), this);
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
    this.availableIds.add(entity.getId());
    this.componentsByEntity.remove(entity);
  }

  @Override
  public Set<Entity> getEntitiesWithComponents(List<Class<? extends Component>> componentClass) {
    return this.componentsByEntity.entrySet().stream()
        .filter(
            entry ->
                componentClass.stream()
                    .allMatch(cc -> entry.getValue().stream().anyMatch(cc::isInstance)))
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
  }

  @Override
  public void addSystem(GameSystem system) {
    this.systems.add(system);
  }

  @Override
  public void update(float deltaTime) {
    System.out.println("World state dump:");
    this.componentsByEntity.forEach(
        (entity, comps) -> {
          final var id = entity.getId();
          System.out.println(" - Entity " + id + ":");
          if (comps == null || comps.isEmpty()) {
            System.out.println("     (no components)");
          } else {
            comps.forEach(
                c -> System.out.println("     - " + c.getClass().getSimpleName() + " => " + c));
          }
        });
    for (final var system : this.systems) {
      system.update(this, deltaTime);
    }
  }
}
