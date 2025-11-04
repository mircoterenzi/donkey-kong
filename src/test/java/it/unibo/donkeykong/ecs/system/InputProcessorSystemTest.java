package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.core.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

import it.unibo.donkeykong.core.WorldImpl;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.StateComponent.*;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InputProcessorSystemTest {

  private static final long DELTA_TIME_IGNORED = 0L;

  private World world;
  private Entity player;
  private Input playerInput;
  private Velocity initialVelocity;
  private StateComponent initialPlayerState;
  private Position initialPosition;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    world.addSystem(new InputProcessorSystem());

    playerInput = new Input();
    initialVelocity = new Velocity(0.0, 0.0);
    initialPlayerState = new StateComponent(State.IDLE, Direction.LEFT);
    initialPosition = new Position(10, 10);

    player =
        world
            .createEntity()
            .addComponent(playerInput)
            .addComponent(initialVelocity)
            .addComponent(initialPlayerState)
            .addComponent(initialPosition)
            .addComponent(new Gravity(GRAVITY));
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

  private Position getPositionComponent(Entity entity) {
    return entity
        .getComponent(Position.class)
        .orElseThrow(() -> new AssertionError("Position component missing"));
  }

  private void simulateGrounded() {
    Entity ground = world.createEntity().addComponent(new SolidComponent());
    player.addComponent(new CollisionEvent(ground));
  }

  private void simulateClimbing() {
    Entity ladder =
        world
            .createEntity()
            .addComponent(new Climbable())
            .addComponent(new Position(10, 10))
            .addComponent(new RectangleCollider(30, 50));
    player.addComponent(new CollisionEvent(ladder));
  }

  @Test
  void testIdleAtSpawn() {
    Velocity velocity = getVelocityComponent(player);
    StateComponent state = getStateComponent(player);

    assertEquals(initialVelocity, velocity, "Initial horizontal velocity should be zero");
    assertEquals(initialPlayerState, state, "Initial state should be IDLE and direction LEFT");
  }

  @Test
  void testHorizontalMovement() {
    playerInput.setCurrentHInput(Input.HorizontalInput.MOVE_RIGHT);
    world.update(DELTA_TIME_IGNORED);

    Velocity updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);

    assertEquals(
        PLAYER_VELOCITY, updatedVelocity.dx(), "Horizontal velocity should match PLAYER_VELOCITY");
    assertEquals(0, updatedVelocity.dy(), "Vertical velocity should remain zero");
    assertEquals(Direction.RIGHT, updatedState.direction(), "Direction should be RIGHT");

    playerInput.setCurrentHInput(Input.HorizontalInput.MOVE_LEFT);
    world.update(DELTA_TIME_IGNORED);

    updatedVelocity = getVelocityComponent(player);
    updatedState = getStateComponent(player);

    assertEquals(
        -PLAYER_VELOCITY,
        updatedVelocity.dx(),
        "Horizontal velocity should be negative PLAYER_VELOCITY");
    assertEquals(0, updatedVelocity.dy(), "Vertical velocity should remain zero");
    assertEquals(Direction.LEFT, updatedState.direction(), "Direction should be LEFT");

    playerInput.setCurrentHInput(Input.HorizontalInput.NONE);
    world.update(DELTA_TIME_IGNORED);

    updatedVelocity = getVelocityComponent(player);

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should be zero");
    assertEquals(0, updatedVelocity.dy(), "Vertical velocity should remain zero");
  }

  @Test
  void testJump() {
    simulateGrounded();
    playerInput.setJumpPressed(true);
    world.update(DELTA_TIME_IGNORED);

    Velocity updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);
    Gravity gravity = player.getComponent(Gravity.class).orElseThrow();

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(
        JUMP_FACTOR * -gravity.gravity(),
        updatedVelocity.dy(),
        "Vertical velocity should match jump calculation");
    assertEquals(State.JUMP, updatedState.state(), "State should be JUMP");
    assertFalse(playerInput.isJumpPressed(), "Jump input should be consumed");
  }

  @Test
  void testJumpAirborne() {
    playerInput.setJumpPressed(true);
    world.update(DELTA_TIME_IGNORED);

    Velocity updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(0, updatedVelocity.dy(), "Vertical velocity should remain zero");
    assertEquals(initialPlayerState, updatedState, "State should remain the same");
    assertFalse(playerInput.isJumpPressed(), "Jump input should be consumed");
  }

  @Test
  void testClimbing() {
    simulateClimbing();
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_UP);
    world.update(DELTA_TIME_IGNORED);

    Velocity updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);
    Gravity gravity = player.getComponent(Gravity.class).orElseThrow();

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(
        -(gravity.gravity() + PLAYER_VELOCITY),
        updatedVelocity.dy(),
        "Vertical velocity should match climbing up calculation");
    assertEquals(State.UP, updatedState.state(), "State should be UP");

    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_DOWN);
    world.update(DELTA_TIME_IGNORED);

    updatedVelocity = getVelocityComponent(player);
    updatedState = getStateComponent(player);

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(
        -gravity.gravity() + PLAYER_VELOCITY,
        updatedVelocity.dy(),
        "Vertical velocity should match climbing down calculation");
    assertEquals(State.DOWN, updatedState.state(), "State should be DOWN");

    playerInput.setCurrentVInput(Input.VerticalInput.NONE);
    world.update(DELTA_TIME_IGNORED);

    updatedVelocity = getVelocityComponent(player);
    updatedState = getStateComponent(player);

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(
        -gravity.gravity(), updatedVelocity.dy(), "Vertical velocity should match gravity only");
    assertEquals(
        State.STOP_CLIMB,
        updatedState.state(),
        "State should be STOP_CLIMB when not moving on a ladder");
  }

  @Test
  void testGrounded() {
    simulateGrounded();
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_UP);
    world.update(DELTA_TIME_IGNORED);

    Velocity updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(0, updatedVelocity.dy(), "Vertical velocity should remain zero when grounded");
    assertEquals(
        State.IDLE, updatedState.state(), "State should remain IDLE when grounded and not moving");

    playerInput.setCurrentHInput(Input.HorizontalInput.MOVE_RIGHT);
    world.update(DELTA_TIME_IGNORED);

    updatedVelocity = getVelocityComponent(player);
    updatedState = getStateComponent(player);
    assertEquals(
        PLAYER_VELOCITY, updatedVelocity.dx(), "Horizontal velocity should match PLAYER_VELOCITY");
    assertEquals(0, updatedVelocity.dy(), "Vertical velocity should remain zero when grounded");
    assertEquals(
        State.MOVING, updatedState.state(), "State should be MOVING when moving horizontally");

    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_DOWN);
    world.update(DELTA_TIME_IGNORED);

    updatedVelocity = getVelocityComponent(player);
    assertEquals(0, updatedVelocity.dy(), "Vertical velocity should remain zero when grounded");
  }

  @Test
  void testFastFall() {
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_DOWN);
    world.update(DELTA_TIME_IGNORED);

    Velocity updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);
    Gravity gravity = player.getComponent(Gravity.class).orElseThrow();

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(
        FALL_FACTOR * gravity.gravity(),
        updatedVelocity.dy(),
        "Vertical velocity should match FALL_FACTOR calculation");
    assertEquals(State.FALL, updatedState.state(), "State should be FALL");
  }

  @Test
  void testAirborneNoInput() {
    Gravity gravity = player.getComponent(Gravity.class).orElseThrow();
    player.updateComponent(initialVelocity, new Velocity(0.0, -gravity.gravity()));
    world.update(DELTA_TIME_IGNORED);

    Velocity updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(
        -gravity.gravity(),
        updatedVelocity.dy(),
        "Vertical velocity should remain unchanged if less or equal than gravity");
    assertEquals(
        initialPlayerState,
        updatedState,
        "State should remain the same when airborne with no input");
  }

  @Test
  void testFastFallThenNormalFall() {
    Gravity gravity = player.getComponent(Gravity.class).orElseThrow();
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_DOWN);
    world.update(DELTA_TIME_IGNORED);

    Velocity updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(
        FALL_FACTOR * gravity.gravity(),
        updatedVelocity.dy(),
        "Vertical velocity should match FALL_FACTOR calculation");
    assertEquals(State.FALL, updatedState.state(), "State should be FALL");

    playerInput.setCurrentVInput(Input.VerticalInput.NONE);
    world.update(DELTA_TIME_IGNORED);

    updatedVelocity = getVelocityComponent(player);

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(
        gravity.gravity(),
        updatedVelocity.dy(),
        "Vertical velocity should return normal gravity after fast fall");
  }
}
