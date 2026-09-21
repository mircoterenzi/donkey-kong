package it.unibo.donkeykong.ecs.system;

import static org.junit.jupiter.api.Assertions.assertTrue;

import it.unibo.donkeykong.core.WorldImpl;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.CollisionEventComponent;
import it.unibo.donkeykong.ecs.component.GoalComponent;
import it.unibo.donkeykong.ecs.component.InputComponent;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WinSystemTest {
  private World world;
  private boolean isWinnerNotified = false;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    world.addSystem(new WinSystem(entity -> isWinnerNotified = true));
  }

  @Test
  void testWinningCollisionTriggersCallback() {
    Entity player = world.createEntity().addComponent(new InputComponent());
    Entity paulineGoal = world.createEntity().addComponent(new GoalComponent());

    player.addComponent(new CollisionEventComponent(paulineGoal));

    world.update(0.1f);

    assertTrue(
        isWinnerNotified,
        "La callback di vittoria non è stata chiamata in seguito alla collisione col GoalComponent.");
  }
}
