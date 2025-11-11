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

public class InputSystemTest {

  private static final long DELTA_TIME_IGNORED = 0L;

  private World world;
  private Entity player;
  private InputComponent playerInput;
  private VelocityComponent initialVelocity;
  private StateComponent initialPlayerState;
  private PositionComponent initialPosition;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    world.addSystem(new ClimbingSystem());
    world.addSystem(new InputSystem());

    playerInput = new InputComponent();
    initialVelocity = new VelocityComponent(0.0, 0.0);
    initialPlayerState = new StateComponent(State.IDLE, Direction.LEFT);
    initialPosition = new PositionComponent(10, 10);

    player =
        world
            .createEntity()
            .addComponent(playerInput)
            .addComponent(initialVelocity)
            .addComponent(initialPlayerState)
            .addComponent(initialPosition)
            .addComponent(new GravityComponent(GRAVITY))
            .addComponent(new RectangleCollider(PLAYER_COLLISION_WIDTH, PLAYER_COLLISION_HEIGHT));
  }

  private VelocityComponent getVelocityComponent(Entity entity) {
    return entity
        .getComponent(VelocityComponent.class)
        .orElseThrow(() -> new AssertionError("VelocityComponent component missing"));
  }

  private StateComponent getStateComponent(Entity entity) {
    return entity
        .getComponent(StateComponent.class)
        .orElseThrow(() -> new AssertionError("StateComponent missing"));
  }

  private PositionComponent getPositionComponent(Entity entity) {
    return entity
        .getComponent(PositionComponent.class)
        .orElseThrow(() -> new AssertionError("PositionComponent component missing"));
  }

  private void simulateGrounded() {
    Entity ground = world.createEntity().addComponent(new SolidComponent());
    player.addComponent(new CollisionEventComponent(ground));
  }

  private Entity simulateClimbing() {
    Entity ladder =
        world
            .createEntity()
            .addComponent(new ClimbableComponent())
            .addComponent(new PositionComponent(10, 10))
            .addComponent(new RectangleCollider(30, 50));
    player.addComponent(new CollisionEventComponent(ladder));
    return ladder;
  }

  @Test
  void testIdleAtSpawn() {
    VelocityComponent velocity = getVelocityComponent(player);
    StateComponent state = getStateComponent(player);

    assertEquals(initialVelocity, velocity, "Initial horizontal velocity should be zero");
    assertEquals(initialPlayerState, state, "Initial state should be IDLE and direction LEFT");
  }

  @Test
  void testHorizontalMovement() {
    playerInput.setCurrentHInput(InputComponent.HorizontalInput.MOVE_RIGHT);
    world.update(DELTA_TIME_IGNORED);

    VelocityComponent updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);

    assertEquals(
        PLAYER_VELOCITY, updatedVelocity.dx(), "Horizontal velocity should match PLAYER_VELOCITY");
    assertEquals(0, updatedVelocity.dy(), "Vertical velocity should remain zero");
    assertEquals(Direction.RIGHT, updatedState.direction(), "Direction should be RIGHT");

    playerInput.setCurrentHInput(InputComponent.HorizontalInput.MOVE_LEFT);
    world.update(DELTA_TIME_IGNORED);

    updatedVelocity = getVelocityComponent(player);
    updatedState = getStateComponent(player);

    assertEquals(
        -PLAYER_VELOCITY,
        updatedVelocity.dx(),
        "Horizontal velocity should be negative PLAYER_VELOCITY");
    assertEquals(0, updatedVelocity.dy(), "Vertical velocity should remain zero");
    assertEquals(Direction.LEFT, updatedState.direction(), "Direction should be LEFT");

    playerInput.setCurrentHInput(InputComponent.HorizontalInput.NONE);
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

    VelocityComponent updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);
    GravityComponent gravity = player.getComponent(GravityComponent.class).orElseThrow();

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

    VelocityComponent updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(0, updatedVelocity.dy(), "Vertical velocity should remain zero");
    assertEquals(initialPlayerState, updatedState, "State should remain the same");
    assertFalse(playerInput.isJumpPressed(), "Jump input should be consumed");
  }

  @Test
  void testClimbing() {
    var ladder = simulateClimbing();
    var ladderPos = ladder.getComponent(PositionComponent.class).orElseThrow();
    playerInput.setCurrentVInput(InputComponent.VerticalInput.MOVE_UP);
    world.update(DELTA_TIME_IGNORED);

    VelocityComponent updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);
    PositionComponent updatedPosition = getPositionComponent(player);
    GravityComponent gravity = player.getComponent(GravityComponent.class).orElseThrow();

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(
        -(gravity.gravity() + PLAYER_VELOCITY),
        updatedVelocity.dy(),
        "Vertical velocity should match climbing up calculation");
    assertEquals(State.UP, updatedState.state(), "State should be UP");
    assertEquals(updatedPosition.x(), ladderPos.x(), "Player should be aligned with ladder");

    playerInput.setCurrentVInput(InputComponent.VerticalInput.MOVE_DOWN);
    world.update(DELTA_TIME_IGNORED);

    updatedVelocity = getVelocityComponent(player);
    updatedState = getStateComponent(player);
    updatedPosition = getPositionComponent(player);

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(
        -gravity.gravity() + PLAYER_VELOCITY,
        updatedVelocity.dy(),
        "Vertical velocity should match climbing down calculation");
    assertEquals(State.DOWN, updatedState.state(), "State should be DOWN");
    assertEquals(updatedPosition.x(), ladderPos.x(), "Player should be aligned with ladder");

    playerInput.setCurrentVInput(InputComponent.VerticalInput.NONE);
    world.update(DELTA_TIME_IGNORED);

    updatedVelocity = getVelocityComponent(player);
    updatedState = getStateComponent(player);
    updatedPosition = getPositionComponent(player);

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(
        -gravity.gravity(), updatedVelocity.dy(), "Vertical velocity should match gravity only");
    assertEquals(
        State.STOP_CLIMB,
        updatedState.state(),
        "State should be STOP_CLIMB when not moving on a ladder");
    assertEquals(updatedPosition.x(), ladderPos.x(), "Player should be aligned with ladder");
  }

  @Test
  void testDetachFromLadder() {
    var ladder = simulateClimbing();
    var ladderPos = ladder.getComponent(PositionComponent.class).orElseThrow().x();
    var ladderCollider = ladder.getComponent(RectangleCollider.class).orElseThrow();
    var ladderEdge = ladderPos + (ladderCollider.width() / 2.0);
    player.updateComponent(new StateComponent(State.UP, Direction.RIGHT));
    playerInput.setCurrentHInput(InputComponent.HorizontalInput.MOVE_RIGHT);
    world.update(DELTA_TIME_IGNORED);

    VelocityComponent updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);
    PositionComponent updatedPosition = getPositionComponent(player);

    assertEquals(PLAYER_VELOCITY, updatedVelocity.dx(), "Player should be moving horizontally");
    assertEquals(ladderEdge, updatedPosition.x(), "Player should snap to ladder edge");
    assertEquals(
        new StateComponent(State.FALL, Direction.RIGHT),
        updatedState,
        "State should be FALL after detaching from ladder");
  }

  @Test
  void testGrounded() {
    simulateGrounded();
    playerInput.setCurrentVInput(InputComponent.VerticalInput.MOVE_UP);
    world.update(DELTA_TIME_IGNORED);

    VelocityComponent updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(0, updatedVelocity.dy(), "Vertical velocity should remain zero when grounded");
    assertEquals(
        State.IDLE, updatedState.state(), "State should remain IDLE when grounded and not moving");

    playerInput.setCurrentHInput(InputComponent.HorizontalInput.MOVE_RIGHT);
    world.update(DELTA_TIME_IGNORED);

    updatedVelocity = getVelocityComponent(player);
    updatedState = getStateComponent(player);
    assertEquals(
        PLAYER_VELOCITY, updatedVelocity.dx(), "Horizontal velocity should match PLAYER_VELOCITY");
    assertEquals(0, updatedVelocity.dy(), "Vertical velocity should remain zero when grounded");
    assertEquals(
        State.MOVING, updatedState.state(), "State should be MOVING when moving horizontally");

    playerInput.setCurrentVInput(InputComponent.VerticalInput.MOVE_DOWN);
    world.update(DELTA_TIME_IGNORED);

    updatedVelocity = getVelocityComponent(player);
    assertEquals(0, updatedVelocity.dy(), "Vertical velocity should remain zero when grounded");
  }

  @Test
  void testAirborneNoInput() {
    GravityComponent gravity = player.getComponent(GravityComponent.class).orElseThrow();
    player.updateComponent(new VelocityComponent(0.0, gravity.gravity()));
    world.update(DELTA_TIME_IGNORED);

    VelocityComponent updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(
        gravity.gravity(),
        updatedVelocity.dy(),
        "Vertical velocity should remain unchanged if less or equal than gravity");
    assertEquals(
        State.FALL,
        updatedState.state(),
        "State should be FALL when airborne with no input falling");
  }

  @Test
  void testFastFall() {
    GravityComponent gravity = player.getComponent(GravityComponent.class).orElseThrow();
    playerInput.setCurrentVInput(InputComponent.VerticalInput.MOVE_DOWN);
    world.update(DELTA_TIME_IGNORED);

    VelocityComponent updatedVelocity = getVelocityComponent(player);
    StateComponent updatedState = getStateComponent(player);

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(
        FALL_FACTOR * gravity.gravity(),
        updatedVelocity.dy(),
        "Vertical velocity should match FALL_FACTOR calculation");
    assertEquals(State.FAST_FALL, updatedState.state(), "State should be FAST_FALL");

    playerInput.setCurrentVInput(InputComponent.VerticalInput.NONE);
    world.update(DELTA_TIME_IGNORED);

    updatedVelocity = getVelocityComponent(player);
    updatedState = getStateComponent(player);

    assertEquals(0, updatedVelocity.dx(), "Horizontal velocity should remain zero");
    assertEquals(
        gravity.gravity(),
        updatedVelocity.dy(),
        "Vertical velocity should return normal gravity after fast fall");
    assertEquals(
        State.FALL, updatedState.state(), "State should return FALL after releasing fast fall");
  }
}
