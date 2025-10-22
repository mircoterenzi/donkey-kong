package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.utilities.Constants.DX_PLAYER_VELOCITY;
import static it.unibo.donkeykong.utilities.Constants.DY_PLAYER_VELOCITY;
import static it.unibo.donkeykong.utilities.Constants.GRAVITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.WorldImpl;
import it.unibo.donkeykong.ecs.component.Climbable;
import it.unibo.donkeykong.ecs.component.CollisionEvent;
import it.unibo.donkeykong.ecs.component.Input;
import it.unibo.donkeykong.ecs.component.Position;
import it.unibo.donkeykong.ecs.component.Velocity;
import it.unibo.donkeykong.ecs.entity.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InputProcessorSystemTest {

  private static final long DELTA_TIME_IGNORED = 0L;
  private static final double INITIAL_DY = 10.0;
  private static final double JUMP_SPEED = -DY_PLAYER_VELOCITY * 2;
  private static final double FAST_FALL_SPEED = DY_PLAYER_VELOCITY * GRAVITY;

  private World world;
  private Entity player;
  private Input playerInput;
  private Velocity initialVelocity;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    world.addSystem(new InputProcessorSystem());

    playerInput = new Input();
    initialVelocity = new Velocity(0, 0);
    player =
        world
            .createEntity()
            .addComponent(playerInput)
            .addComponent(initialVelocity)
            .addComponent(new Position(0, 0));
  }

  private Velocity getVelocityComponent(Entity entity) {
    return entity
        .getComponent(Velocity.class)
        .orElseThrow(() -> new AssertionError("Velocity component missing"));
  }

  private void simulateGrounded() {
    Entity ground = world.createEntity().addComponent(new Position(0, 0));
    ground.addComponent(new CollisionEvent(player));
  }

  private void simulateClimbing() {
    Entity ladder = world.createEntity().addComponent(new Climbable());
    ladder.addComponent(new CollisionEvent(player));
  }

  @Test
  void testMoveRightGrounded() {
    simulateGrounded();
    playerInput.setCurrentHInput(Input.HorizontalInput.MOVE_RIGHT);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(DX_PLAYER_VELOCITY, newVelocity.dx());
    assertEquals(0, newVelocity.dy(), "Player grounded should not move vertically");
    assertNotSame(initialVelocity, newVelocity);
  }

  @Test
  void testMoveLeftGrounded() {
    simulateGrounded();
    playerInput.setCurrentHInput(Input.HorizontalInput.MOVE_LEFT);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(-DX_PLAYER_VELOCITY, newVelocity.dx());
    assertEquals(0, newVelocity.dy());
  }

  @Test
  void testJumpGrounded() {
    simulateGrounded();
    playerInput.setJumpPressed(true);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(0, newVelocity.dx());
    assertEquals(JUMP_SPEED, newVelocity.dy(), "Player should jump");
    assertFalse(playerInput.isJumpPressed(), "Jump press should be consumed");
  }

  @Test
  void testJumpAirborne() {
    initialVelocity = new Velocity(0, INITIAL_DY);
    player.updateComponent(new Velocity(0, 0), initialVelocity);
    playerInput.setJumpPressed(true);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(0, newVelocity.dx());
    assertEquals(
        INITIAL_DY, newVelocity.dy(), "Player should not jump if airborne (dy remains old dy)");
    assertFalse(playerInput.isJumpPressed(), "Jump press should be consumed");
  }

  @Test
  void testClimbUp() {
    simulateClimbing();
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_UP);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(0, newVelocity.dx());
    assertEquals(-DY_PLAYER_VELOCITY, newVelocity.dy(), "Player should move up");
  }

  @Test
  void testClimbDown() {
    simulateClimbing();
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_DOWN);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(0, newVelocity.dx());
    assertEquals(DY_PLAYER_VELOCITY, newVelocity.dy(), "Player should move down");
  }

  @Test
  void testStopClimbing() {
    simulateClimbing();
    playerInput.setCurrentVInput(Input.VerticalInput.NONE);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(0, newVelocity.dx());
    assertEquals(0, newVelocity.dy(), "Player should stop vertical movement");
  }

  @Test
  void testJumpWhileClimbing() {
    simulateClimbing();
    playerInput.setJumpPressed(true);
    playerInput.setCurrentHInput(Input.HorizontalInput.MOVE_RIGHT); // Input orizzontale
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_UP); // Input verticale
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(DX_PLAYER_VELOCITY, newVelocity.dx(), "Horizontal input should be processed");
    assertEquals(JUMP_SPEED, newVelocity.dy(), "Player should jump off the ladder");
    assertFalse(playerInput.isJumpPressed(), "Jump press should be consumed");
  }

  @Test
  void testFastFallAirborne() {
    initialVelocity = new Velocity(0, INITIAL_DY);
    player.updateComponent(new Velocity(0, 0), initialVelocity);
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_DOWN);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(0, newVelocity.dx());
    assertEquals(FAST_FALL_SPEED, newVelocity.dy(), "Player should fast fall when in air");
  }

  @Test
  void testFastFallGrounded() {
    simulateGrounded();
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_DOWN);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(0, newVelocity.dx());
    assertEquals(0, newVelocity.dy(), "Player should not fast fall when grounded (dy should be 0)");
  }

  @Test
  void testAirborneNoInput() {
    initialVelocity = new Velocity(0, INITIAL_DY);
    player.updateComponent(new Velocity(0, 0), initialVelocity);
    playerInput.setCurrentHInput(Input.HorizontalInput.NONE);
    playerInput.setCurrentVInput(Input.VerticalInput.NONE);
    playerInput.setJumpPressed(false);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(0, newVelocity.dx());
    assertEquals(INITIAL_DY, newVelocity.dy(), "Vertical velocity should be preserved if airborne");
  }

  @Test
  void testClimbingHasPriorityOverGrounded() {
    simulateGrounded();
    simulateClimbing();
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_UP); // Prova a salire

    world.update(DELTA_TIME_IGNORED);

    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(0, newVelocity.dx());
    assertEquals(
        -DY_PLAYER_VELOCITY,
        newVelocity.dy(),
        "Climbing (dy=-VEL) should have priority over grounded state (dy=0)");
  }
}
