package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.ecs.component.StateComponent.Direction.*;
import static it.unibo.donkeykong.ecs.component.StateComponent.State.*;
import static it.unibo.donkeykong.utilities.Constants.*;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.StateComponent.*;
import it.unibo.donkeykong.ecs.entity.Entity;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** System that processes player input and updates entity velocities accordingly. */
public class InputProcessorSystem implements GameSystem {
  @Override
  public void update(World world, float deltaTime) {
    world
        .getEntitiesWithComponents(List.of(Input.class))
        .forEach(
            entity -> {
              Input input = entity.getComponent(Input.class).orElseThrow();
              Velocity oldVelocity = entity.getComponent(Velocity.class).orElseThrow();
              StateComponent oldState = entity.getComponent(StateComponent.class).orElseThrow();
              Optional<CollisionEvent> collisionEvent = entity.getComponent(CollisionEvent.class);
              Gravity gravity = entity.getComponent(Gravity.class).orElse(new Gravity(0));

              double newDx, newDy;
              State newState;
              Direction newDir;
              Optional<Entity> ladder = collisionEvent
                      .flatMap(event -> event.getCollisionsWith(Climbable.class).stream().findFirst());
              final boolean isClimbing = calculateDelta(ladder, entity);
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
              }

              if (input.isJumpPressed()) {
                if (isGrounded || isClimbing) {
                  newDy = JUMP_FACTOR * gravity.gravity();
                  newState = JUMP;
                } else {
                  newDy = oldVelocity.dy();
                  newState = oldState.state();
                }
                input.setJumpPressed(false);
              } else if (isClimbing) {
                switch (input.getCurrentVInput()) {
                  case MOVE_UP -> {
                    newDy = -(gravity.gravity() + PLAYER_VELOCITY);
                    newState = UP;
                  }
                  case MOVE_DOWN -> {
                    newDy = -gravity.gravity() + PLAYER_VELOCITY;
                    newState = DOWN;
                  }
                  default -> {
                    newDy = -gravity.gravity();
                    newState = STOP_CLIMB;
                  }
                }
              } else if (isGrounded) {
                newDy = oldVelocity.dy();
                newState = MOVING;
                if (newDx == 0) {
                  newState = IDLE;
                }
              } else if (input.getCurrentVInput() == Input.VerticalInput.MOVE_DOWN) {
                newDy = FALL_FACTOR;
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

  private boolean calculateDelta(Optional<Entity> ladder, Entity entity) {
    if (ladder.isEmpty()) {
      return false;
    }

    Position ladderPos = ladder.get().getComponent(Position.class).orElseThrow();
    Position entityPos = entity.getComponent(Position.class).orElseThrow();
    Optional<Collider> optLadderCollider = ladder.get().getComponent(Collider.class);
    if (optLadderCollider.isEmpty() || !(optLadderCollider.get() instanceof RectangleCollider ladderCollider)) {
      return false;
    }

    double ladderHalfWidth = ladderCollider.width() / 2.0;
    double horizontalDistance = Math.abs(ladderPos.x() - entityPos.x());
    return horizontalDistance < ladderHalfWidth;
  }
}
