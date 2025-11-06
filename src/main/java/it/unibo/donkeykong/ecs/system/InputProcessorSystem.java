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
import it.unibo.donkeykong.ecs.system.common.CollisionUtils;
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
              final boolean canClimb = CollisionUtils.areAligned(entity, ladder);
              final boolean isGrounded =
                  collisionEvent
                      .map(event -> event.hasCollisionsWith(SolidComponent.class))
                      .orElse(false);
              final boolean wasClimbing =
                  oldState.state() == UP
                      || oldState.state() == DOWN
                      || oldState.state() == STOP_CLIMB;

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
                if (isGrounded || canClimb) {
                  newDy = JUMP_FACTOR * -gravity.gravity();
                  newState = JUMP;
                } else {
                  newDy = oldVelocity.dy();
                  newState = oldState.state();
                }
                input.setJumpPressed(false);
              } else if (canClimb) {
                if (newDx == 0) {
                  snapToLadderCenter(entity, ladder);
                  switch (input.getCurrentVInput()) {
                    case MOVE_UP -> {
                      newDy = oldVelocity.dy() - PLAYER_VELOCITY;
                      newState = UP;
                    }
                    case MOVE_DOWN -> {
                      newDy = oldVelocity.dy() + PLAYER_VELOCITY;
                      newState = DOWN;
                    }
                    default -> {
                      if(oldState.state() != JUMP && oldState.state() != FALL && oldState.state() != FAST_FALL) {
                        newDy = oldVelocity.dy();
                        newState = STOP_CLIMB;
                      } else {
                        newDy = oldVelocity.dy();
                        newState = oldState.state();
                      }
                    }
                  }
                } else {
                  if (wasClimbing) {
                    snapToLadderEdge(entity, ladder, new VelocityComponent(newDx, 0));
                    newState = IDLE;
                  } else {
                    newState = oldState.state();
                  }
                  newDy = oldVelocity.dy();
                }
              } else if (isGrounded) {
                newDy = oldVelocity.dy();
                newState = newDx == 0 ? IDLE : MOVING;
              } else if (input.getCurrentVInput() == InputComponent.VerticalInput.MOVE_DOWN) {
                newDy = FALL_FACTOR * gravity.gravity();
                newState = FAST_FALL;
              } else {
                newDy = oldVelocity.dy();
                if (oldState.state() == FAST_FALL) {
                  newDy = gravity.gravity();
                }
                newState = newDy < 0 ? JUMP : FALL;
              }

              var state = new StateComponent(newState, newDir);
              entity.updateComponent(oldState, state);
              entity.updateComponent(oldVelocity, new VelocityComponent(newDx, newDy));
            });
  }

  private void snapToLadderCenter(Entity entity, Optional<Entity> ladder) {
    if (ladder.isPresent()) {
      PositionComponent entityPos = entity.getComponent(PositionComponent.class).orElseThrow();
      PositionComponent ladderPos =
          ladder.get().getComponent(PositionComponent.class).orElseThrow();
      entity.updateComponent(entityPos, new PositionComponent(ladderPos.x(), entityPos.y()));
    }
  }

  private void snapToLadderEdge(
      Entity entity, Optional<Entity> ladder, VelocityComponent velocity) {
    if (ladder.isPresent()) {
      PositionComponent entityPos = entity.getComponent(PositionComponent.class).orElseThrow();
      PositionComponent ladderPos =
          ladder.get().getComponent(PositionComponent.class).orElseThrow();
      Collider ladderCollider = ladder.get().getComponent(Collider.class).orElseThrow();

      if (ladderCollider instanceof RectangleCollider rectCollider) {
        final double ladderHalfWidth = rectCollider.width() / 2.0;
        final double newX =
            velocity.dx() < 0 ? ladderPos.x() - ladderHalfWidth : ladderPos.x() + ladderHalfWidth;

        entity.updateComponent(entityPos, new PositionComponent(newX, entityPos.y()));
      }
    }
  }
}
