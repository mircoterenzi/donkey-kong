package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.utilities.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import it.unibo.donkeykong.ecs.*;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InputProcessorSystemTest {

  private static final long DELTA_TIME_IGNORED = 0L;
  private static final double INITIAL_DY = 10.0;

  private static final double JUMP_SPEED = -(GRAVITY + JUMP_FACTOR);
  private static final double CLIMB_UP_SPEED = -(GRAVITY + PLAYER_VELOCITY);
  private static final double CLIMB_DOWN_SPEED = -GRAVITY + PLAYER_VELOCITY;
  private static final double STOP_CLIMB_SPEED = -GRAVITY;
  private static final double GROUNDED_DY = -GRAVITY;

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
    PlayerState initialPlayerState = new PlayerState(PlayerState.State.STOP_GROUND);

    player =
        world
            .createEntity()
            .addComponent(playerInput)
            .addComponent(initialVelocity)
            .addComponent(new Position(0, 0))
            .addComponent(initialPlayerState);
  }

  private Velocity getVelocityComponent(Entity entity) {
    return entity
        .getComponent(Velocity.class)
        .orElseThrow(() -> new AssertionError("Velocity component missing"));
  }

  private void simulateGrounded() {
    Entity ground = world.createEntity().addComponent(new GroundComponent());
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
    assertEquals(PLAYER_VELOCITY, newVelocity.dx());
    assertEquals(GROUNDED_DY, newVelocity.dy(), "Player grounded should be affected by gravity");
    assertNotSame(initialVelocity, newVelocity);
  }

  @Test
  void testMoveLeftGrounded() {
    simulateGrounded();
    playerInput.setCurrentHInput(Input.HorizontalInput.MOVE_LEFT);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(-PLAYER_VELOCITY, newVelocity.dx());
    assertEquals(GROUNDED_DY, newVelocity.dy());
  }

  @Test
  void testJumpGrounded() {
    simulateGrounded();
    playerInput.setJumpPressed(true);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(0, newVelocity.dx());
    assertEquals(JUMP_SPEED, newVelocity.dy(), "Player should jump with new speed");
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
    assertEquals(CLIMB_UP_SPEED, newVelocity.dy(), "Player should move up");
  }

  @Test
  void testClimbDown() {
    simulateClimbing();
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_DOWN);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(0, newVelocity.dx());
    assertEquals(CLIMB_DOWN_SPEED, newVelocity.dy(), "Player should move down");
  }

  @Test
  void testStopClimbing() {
    simulateClimbing();
    playerInput.setCurrentVInput(Input.VerticalInput.NONE);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(0, newVelocity.dx());
    assertEquals(
        STOP_CLIMB_SPEED,
        newVelocity.dy(),
        "Player should stop vertical movement but be affected by gravity");
  }

  @Test
  void testJumpWhileClimbing() {
    simulateClimbing();
    playerInput.setJumpPressed(true);
    playerInput.setCurrentHInput(Input.HorizontalInput.MOVE_RIGHT); // Input orizzontale
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_UP); // Input verticale
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(PLAYER_VELOCITY, newVelocity.dx(), "Horizontal input should be processed");
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
    double expectedFallSpeed = INITIAL_DY + FALL_FACTOR; // 10.0 + 3.0
    assertEquals(0, newVelocity.dx());
    assertEquals(expectedFallSpeed, newVelocity.dy(), "Player should fast fall when in air");
  }

  @Test
  void testFastFallGrounded() {
    simulateGrounded();
    playerInput.setCurrentVInput(Input.VerticalInput.MOVE_DOWN);
    world.update(DELTA_TIME_IGNORED);
    Velocity newVelocity = getVelocityComponent(player);
    assertEquals(0, newVelocity.dx());
    assertEquals(
        GROUNDED_DY,
        newVelocity.dy(),
        "Player should not fast fall when grounded (dy should be gravity)");
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
        CLIMB_UP_SPEED,
        newVelocity.dy(),
        "Climbing (dy=CLIMB_UP_SPEED) should have priority over grounded state (dy=GROUNDED_DY)");
  }
}
