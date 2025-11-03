package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.utilities.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import it.unibo.donkeykong.ecs.*;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.StateComponent.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InputProcessorSystemTest {

  private static final long DELTA_TIME_IGNORED = 0L;
  private static final double INITIAL_DY = 0.0;
  private static final double INITIAL_DX = 0.0;

  private World world;
  private Entity player;
  private Input playerInput;
  private Velocity initialVelocity;
  private StateComponent initialPlayerState;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    world.addSystem(new InputProcessorSystem());

    playerInput = new Input();
    initialVelocity = new Velocity(INITIAL_DX, INITIAL_DY);
    initialPlayerState = new StateComponent(State.IDLE, Direction.LEFT);

    player =
        world
            .createEntity()
            .addComponent(playerInput)
            .addComponent(initialVelocity)
            .addComponent(initialPlayerState)
            .addComponent(new Gravity(GRAVITY)); // System requires Gravity
  }

  private Velocity getVelocityComponent(Entity entity) {
    return entity
        .getComponent(Velocity.class)
        .orElseThrow(() -> new AssertionError("Velocity component missing"));
  }

  private StateComponent getStateComponent(Entity entity) {
    return entity
        .getComponent(StateComponent.class)
        .orElseThrow(() -> new AssertionError("StateComponent missing"));
  }

  private void simulateGrounded() {
    Entity ground = world.createEntity().addComponent(new SolidComponent());
    player.addComponent(new CollisionEvent(ground));
  }

  private void simulateClimbing() {
    Entity ladder = world.createEntity().addComponent(new Climbable());
    player.addComponent(new CollisionEvent(ladder));
  }

  @Test
  void testMoveRightGrounded() {
    simulateGrounded();
    playerInput.setCurrentHInput(Input.HorizontalInput.MOVE_RIGHT);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    StateComponent newState = getStateComponent(player);

    assertEquals(PLAYER_VELOCITY, newVelocity.dx());
    assertEquals(
        INITIAL_DY,
        newVelocity.dy(),
        "Player grounded dy should be old dy (gravity is not applied by this system)");
    assertEquals(State.MOVING, newState.state());
    assertEquals(Direction.RIGHT, newState.direction());
    assertNotSame(initialVelocity, newVelocity);
    assertNotSame(initialPlayerState, newState);
  }

  @Test
  void testMoveLeftGrounded() {
    simulateGrounded();
    playerInput.setCurrentHInput(Input.HorizontalInput.MOVE_LEFT);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    StateComponent newState = getStateComponent(player);

    assertEquals(-PLAYER_VELOCITY, newVelocity.dx());
    assertEquals(INITIAL_DY, newVelocity.dy());
    assertEquals(State.MOVING, newState.state());
    assertEquals(Direction.LEFT, newState.direction());
  }

  @Test
  void testIdleGrounded() {
    simulateGrounded();
    playerInput.setCurrentHInput(Input.HorizontalInput.NONE);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    StateComponent newState = getStateComponent(player);

    assertEquals(0, newVelocity.dx());
    assertEquals(INITIAL_DY, newVelocity.dy());
    assertEquals(State.IDLE, newState.state());
  }

  @Test
  void testJumpGrounded() {
    simulateGrounded();
    playerInput.setJumpPressed(true);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    StateComponent newState = getStateComponent(player);

    assertEquals(0, newVelocity.dx());
    assertEquals(JUMP_FACTOR, newVelocity.dy(), "Player should jump with JUMP_FACTOR speed");
    assertEquals(State.JUMP, newState.state());
    assertFalse(playerInput.isJumpPressed(), "Jump press should be consumed");
  }

  @Test
  void testJumpAirborne() {
    playerInput.setJumpPressed(true);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    StateComponent newState = getStateComponent(player);

    assertEquals(INITIAL_DX, newVelocity.dx());
    assertEquals(
        INITIAL_DY, newVelocity.dy(), "Player should not jump if airborne (dy remains old dy)");
    assertEquals(initialPlayerState.state(), newState.state(), "State should be unchanged");
    assertFalse(playerInput.isJumpPressed(), "Jump press should be consumed");
  }

  @Test
  void testClimbUp() {
    simulateClimbing();
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_UP);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    StateComponent newState = getStateComponent(player);

    assertEquals(0, newVelocity.dx());
    assertEquals(-(GRAVITY + PLAYER_VELOCITY), newVelocity.dy(), "Player should move up");
    assertEquals(State.UP, newState.state());
  }

  @Test
  void testClimbDown() {
    simulateClimbing();
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_DOWN);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    StateComponent newState = getStateComponent(player);

    assertEquals(0, newVelocity.dx());
    assertEquals(-GRAVITY + PLAYER_VELOCITY, newVelocity.dy(), "Player should move down");
    assertEquals(State.DOWN, newState.state());
  }

  @Test
  void testStopClimbing() {
    simulateClimbing();
    playerInput.setCurrentVInput(Input.VerticalInput.NONE);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    StateComponent newState = getStateComponent(player);

    assertEquals(0, newVelocity.dx());
    assertEquals(
        -GRAVITY, newVelocity.dy(), "Player should stop vertical movement but negate gravity");
    assertEquals(State.STOP_CLIMB, newState.state());
  }

  @Test
  void testJumpWhileClimbing() {
    simulateClimbing();
    playerInput.setJumpPressed(true);
    playerInput.setCurrentHInput(Input.HorizontalInput.MOVE_RIGHT);
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_UP);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    StateComponent newState = getStateComponent(player);

    assertEquals(PLAYER_VELOCITY, newVelocity.dx(), "Horizontal input should be processed");
    assertEquals(JUMP_FACTOR, newVelocity.dy(), "Player should jump off the ladder");
    assertEquals(State.JUMP, newState.state());
    assertEquals(Direction.RIGHT, newState.direction());
    assertFalse(playerInput.isJumpPressed(), "Jump press should be consumed");
  }

  @Test
  void testFastFallAirborne() {
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_DOWN);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    StateComponent newState = getStateComponent(player);

    assertEquals(0, newVelocity.dx());
    assertEquals(FALL_FACTOR, newVelocity.dy(), "Player should fast fall when in air");
    assertEquals(State.FALL, newState.state());
  }

  @Test
  void testFastFallGrounded() {
    simulateGrounded();
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_DOWN);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    StateComponent newState = getStateComponent(player);

    assertEquals(0, newVelocity.dx());
    assertEquals(
        INITIAL_DY,
        newVelocity.dy(),
        "Player should not fast fall when grounded (dy should be old dy)");
    assertEquals(State.IDLE, newState.state(), "State should be IDLE (or MOVING if dx != 0)");
  }

  @Test
  void testAirborneNoInput() {
    playerInput.setCurrentHInput(Input.HorizontalInput.NONE);
    playerInput.setCurrentVInput(Input.VerticalInput.NONE);
    playerInput.setJumpPressed(false);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    StateComponent newState = getStateComponent(player);

    assertEquals(INITIAL_DX, newVelocity.dx());
    assertEquals(INITIAL_DY, newVelocity.dy(), "Velocity should be preserved if airborne");
    assertEquals(initialPlayerState.state(), newState.state(), "State should be unchanged");
  }
}
