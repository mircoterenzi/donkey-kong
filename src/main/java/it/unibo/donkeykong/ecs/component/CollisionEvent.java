package it.unibo.donkeykong.ecs.component;

import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollisionEvent implements EventComponent {

  private final Set<Entity> entities;

  public CollisionEvent() {
    this.entities = new HashSet<>();
  }

  public CollisionEvent(Entity collision) {
    this();
    this.entities.add(collision);
  }

  public void addCollision(Entity otherEntity) {
    this.entities.remove(otherEntity);
    this.entities.add(otherEntity);
  }

  public boolean hasCollisionsWith(Class<? extends Component> componentClass) {
    return entities.stream().anyMatch(entity -> entity.getComponent(componentClass).isPresent());
  }

  public List<Entity> getCollisionsWith(Class<? extends Component> componentClass) {
    return entities.stream()
        .filter(entity -> entity.getComponent(componentClass).isPresent())
        .toList();
  }
}
