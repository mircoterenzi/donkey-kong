package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.utilities.Constants.*;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.StateComponent.*;
import java.util.List;
import java.util.Optional;

import static it.unibo.donkeykong.ecs.component.StateComponent.State.*;
import static it.unibo.donkeykong.ecs.component.StateComponent.Direction.*;

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
              StateComponent oldState = entity.getComponent(StateComponent.class).orElseThrow();
              Optional<CollisionEvent> collisionEvent = entity.getComponent(CollisionEvent.class);

              double newDx, newDy;
              State newState;
              Direction newDir;
              final boolean isClimbing =
                  collisionEvent
                      .map(event -> event.hasCollisionsWith(Climbable.class))
                      .orElse(false);
              final boolean isGrounded =
                  collisionEvent
                      .map(event -> event.hasCollisionsWith(GroundComponent.class))
                      .orElse(false);

              switch (input.getCurrentHInput()) {
                case MOVE_LEFT -> {
                  newDir = LEFT;
                  newDx = -PLAYER_VELOCITY;
                }
                case MOVE_RIGHT -> {
                  newDir = RIGHT;
                  newDx = PLAYER_VELOCITY;
                }
                default -> {
                  newDir = oldState.direction();
                  newDx = 0;
                }
              };

              if (input.isJumpPressed()) {
                if (isGrounded || isClimbing) {
                  newDy = -(GRAVITY + JUMP_FACTOR);
                  newState = JUMP;
                } else {
                  newDy = oldVelocity.dy();
                  newState = oldState.state();
                }
                input.setJumpPressed(false);
              } else if (isClimbing) {
                switch (input.getCurrentVInput()) {
                  case MOVE_UP -> {
                    newDy = -(GRAVITY + PLAYER_VELOCITY);
                    newState = UP;
                  }
                  case MOVE_DOWN -> {
                    newDy = -GRAVITY + PLAYER_VELOCITY;
                    newState = DOWN;
                  }
                  default -> {
                    newDy = -GRAVITY;
                    newState = STOP_CLIMB;
                  }
                }
              } else if (isGrounded) {
                newDy = -GRAVITY;
                newState = MOVING;
                if (newDx == 0) {
                  newState = IDLE;
                }
              } else if (input.getCurrentVInput() == Input.VerticalInput.MOVE_DOWN) {
                newDy = oldVelocity.dy() + FALL_FACTOR;
                newState = FALL;
              } else {
                newDy = oldVelocity.dy();
                newState = oldState.state();
              }

              var state = new StateComponent(newState, newDir);
              entity.updateComponent(oldState, state);
              entity.updateComponent(oldVelocity, new Velocity(newDx, newDy));
            });
  }
}
