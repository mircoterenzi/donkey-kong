package it.unibo.donkeykong.ecs.system;

import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.donkeykong.core.WorldImpl;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.CollisionEventComponent;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EventDispatchSystemTest {

  private World world;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    world.addSystem(new EventDispatchSystem());
  }

  @Test
  void testEventComponentsAreClearedAfterUpdate() {
    Entity entity = world.createEntity();
    // CollisionEventComponent implementa EventComponent
    entity.addComponent(new CollisionEventComponent());

    world.update(0.1f);

    assertTrue(
        entity.getComponent(CollisionEventComponent.class).isEmpty(),
        "EventComponent deve essere rimosso dall'entità al termine dell'update.");
  }
}
