package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.utilities.Constants.*;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.PlayerState.State;
import java.util.List;

/** System that processes player input and updates entity velocities accordingly. */
public class InputProcessorSystem implements GameSystem {
  @Override
  public void update(World world, long deltaTime) {
    world
        .getEntitiesWithComponents(List.of(Input.class))
        .forEach(
            entity -> {
              Input input = entity.getComponent(Input.class).orElseThrow();
              Velocity oldVelocity = entity.getComponent(Velocity.class).orElseThrow();
              PlayerState oldState = entity.getComponent(PlayerState.class).orElseThrow();

              double newDx, newDy;
              PlayerState newState;
              final boolean isClimbing =
                  world.getEntitiesWithComponents(List.of(Climbable.class)).stream()
                      .anyMatch(
                          e ->
                              e.getComponent(CollisionEvent.class)
                                  .orElseThrow()
                                  .otherEntity()
                                  .equals(entity));
              final boolean isGrounded = entity.getComponent(GroundedEvent.class).isPresent();

              newDx =
                switch (input.getCurrentHInput()) {
                  case MOVE_LEFT -> -PLAYER_VELOCITY;
                  case MOVE_RIGHT -> PLAYER_VELOCITY;
                  default -> 0;
              };

              if (input.isJumpPressed()) {
                if (isGrounded || isClimbing) {
                  newDy = - (GRAVITY + JUMP_FACTOR);
                  newState = new PlayerState(State.JUMP);
                } else {
                  newDy = oldVelocity.dy();
                  newState = oldState;
                }
                input.setJumpPressed(false);
              } else if (isClimbing) {
                  switch (input.getCurrentVInput()) {
                    case MOVE_UP -> {
                      newDy = -(GRAVITY + PLAYER_VELOCITY);
                      newState = new PlayerState(State.CLIMB_UP);
                    }
                    case MOVE_DOWN -> {
                      newDy = - GRAVITY + PLAYER_VELOCITY;
                      newState = new PlayerState(State.CLIMB_DOWN);
                    }
                    default -> {
                      newDy = - GRAVITY;
                      newState = new PlayerState(State.STOP_CLIMB);
                    }
                  };
              } else if (isGrounded) {
                newDy = - GRAVITY;
                if (newDx < 0) {
                  newState = new PlayerState(State.RUN_LEFT);
                } else if (newDx > 0) {
                  newState = new PlayerState(State.RUN_RIGHT);
                } else {
                  newState = new PlayerState(State.STOP_GROUND);
                }
              } else if (input.getCurrentVInput() == Input.VerticalInput.MOVE_DOWN) {
                newDy = oldVelocity.dy() + FALL_FACTOR;
                newState = new PlayerState(State.FALL);
              } else {
                newDy = oldVelocity.dy();
                newState = oldState;
              }

              entity.updateComponent(oldState, newState);
              entity.updateComponent(oldVelocity, new Velocity(newDx, newDy));
            });
  }
}
