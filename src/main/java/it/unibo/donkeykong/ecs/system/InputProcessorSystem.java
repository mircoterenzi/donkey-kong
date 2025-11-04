package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.core.Constants.*;
import static it.unibo.donkeykong.ecs.component.StateComponent.Direction.*;
import static it.unibo.donkeykong.ecs.component.StateComponent.State.*;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.StateComponent.*;
import it.unibo.donkeykong.ecs.component.api.Collider;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import java.util.List;
import java.util.Optional;

/** System that processes player input and updates entity velocities accordingly. */
public class InputProcessorSystem implements GameSystem {
  @Override
  public void update(World world, float deltaTime) {
    world
        .getEntitiesWithComponents(List.of(InputComponent.class))
        .forEach(
            entity -> {
              InputComponent input = entity.getComponent(InputComponent.class).orElseThrow();
              VelocityComponent oldVelocity =
                  entity.getComponent(VelocityComponent.class).orElseThrow();
              StateComponent oldState = entity.getComponent(StateComponent.class).orElseThrow();
              Optional<CollisionEventComponent> collisionEvent =
                  entity.getComponent(CollisionEventComponent.class);
              GravityComponent gravity =
                  entity.getComponent(GravityComponent.class).orElse(new GravityComponent(0));

              double newDx, newDy;
              State newState;
              Direction newDir;
              Optional<Entity> ladder =
                  collisionEvent.flatMap(
                      event ->
                          event.getCollisionsWith(ClimbableComponent.class).stream().findFirst());
              final boolean isClimbing = calculateDelta(ladder, entity);
              final boolean isGrounded =
                  collisionEvent
                      .map(event -> event.hasCollisionsWith(SolidComponent.class))
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
                  newDy = JUMP_FACTOR * -gravity.gravity();
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
                if (newDx == 0) {
                  newState = IDLE;
                } else {
                  newState = MOVING;
                }
              } else if (input.getCurrentVInput() == InputComponent.VerticalInput.MOVE_DOWN) {
                newDy = FALL_FACTOR * gravity.gravity();
                newState = FALL;
              } else {
                newDy = oldVelocity.dy();
                if (oldState.state() == FALL) {
                  newDy = gravity.gravity();
                }
                newState = IDLE;
              }

              var state = new StateComponent(newState, newDir);
              entity.updateComponent(oldState, state);
              entity.updateComponent(oldVelocity, new VelocityComponent(newDx, newDy));
            });
  }

  private boolean calculateDelta(Optional<Entity> ladder, Entity entity) {
    if (ladder.isEmpty()) {
      return false;
    }

    PositionComponent ladderPos = ladder.get().getComponent(PositionComponent.class).orElseThrow();
    PositionComponent entityPos = entity.getComponent(PositionComponent.class).orElseThrow();
    Optional<Collider> optLadderCollider = ladder.get().getComponent(Collider.class);
    if (optLadderCollider.isEmpty()
        || !(optLadderCollider.get() instanceof RectangleCollider ladderCollider)) {
      return false;
    }

    double ladderHalfWidth = ladderCollider.width() / 2.0;
    double horizontalDistance = Math.abs(ladderPos.x() - entityPos.x());
    return horizontalDistance < ladderHalfWidth;
  }
}
