package it.unibo.donkeykong.ecs.system;

import static it.unibo.donkeykong.core.Constants.*;
import static it.unibo.donkeykong.ecs.component.StateComponent.Direction.*;
import static it.unibo.donkeykong.ecs.component.StateComponent.State.*;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.InputComponent.*;
import it.unibo.donkeykong.ecs.component.StateComponent.*;
import it.unibo.donkeykong.ecs.component.api.Collider;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import it.unibo.donkeykong.ecs.system.common.CollisionUtils;
import java.util.List;
import java.util.Optional;

/** System that processes player input and updates entity velocities accordingly. */
public class InputSystem implements GameSystem {
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
              Optional<Entity> ladderOpt =
                  collisionEvent.flatMap(
                      event ->
                          event.getCollisionsWith(ClimbableComponent.class).stream().findFirst());
              final boolean canClimb =
                  ladderOpt.filter(v -> CollisionUtils.areAligned(entity, v)).isPresent();
              final boolean isGrounded =
                  collisionEvent
                      .map(event -> event.hasCollisionsWith(SolidComponent.class))
                      .orElse(false);
              final boolean wasClimbing =
                  oldState.state() == UP
                      || oldState.state() == DOWN
                      || oldState.state() == STOP_CLIMB;
              final boolean wasInAir =
                  oldState.state() == JUMP
                      || oldState.state() == FALL
                      || oldState.state() == FAST_FALL;

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
                if (isGrounded || (canClimb && !wasInAir)) {
                  newDy = JUMP_FACTOR * -gravity.gravity();
                  newState = JUMP;
                } else {
                  newDy = oldVelocity.dy();
                  newState = oldState.state();
                }
                input.setJumpPressed(false);
              } else if (canClimb) {
                if (input.getCurrentHInput() == HorizontalInput.NONE) {
                  snapToLadderCenter(entity, ladderOpt.get());
                  switch (input.getCurrentVInput()) {
                    case MOVE_UP -> {
                      if (isAtTopOfLadder(entity, ladderOpt.get())) {
                        snapToLadderTop(entity, ladderOpt.get());
                        newDy = 0;
                        newState = IDLE;
                      } else {
                        newDy = oldVelocity.dy() - PLAYER_VELOCITY;
                        newState = UP;
                      }
                    }
                    case MOVE_DOWN -> {
                      newDy = oldVelocity.dy() + PLAYER_VELOCITY;
                      newState = DOWN;
                    }
                    default -> {
                      newDy = oldVelocity.dy();
                      if (!wasInAir) {
                        newState = STOP_CLIMB;
                      } else {
                        newState = oldState.state();
                      }
                    }
                  }
                } else {
                  if (wasClimbing) {
                    snapToLadderEdge(entity, ladderOpt.get(), new VelocityComponent(newDx, 0));
                    newState = FALL;
                  } else {
                    newState = oldState.state();
                  }
                  newDy = oldVelocity.dy();
                }
              } else if (isGrounded) {
                newDy = oldVelocity.dy();
                newState = input.getCurrentHInput() == HorizontalInput.NONE ? IDLE : MOVING;
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

              entity.updateComponent(new VelocityComponent(newDx, newDy));
              entity.updateComponent(new StateComponent(newState, newDir));
            });
  }

  private boolean isAtTopOfLadder(Entity entity, Entity ladder) {
    PositionComponent entityPos = entity.getComponent(PositionComponent.class).orElseThrow();
    PositionComponent ladderPos = ladder.getComponent(PositionComponent.class).orElseThrow();
    Collider ladderCollider = ladder.getComponent(Collider.class).orElseThrow();

    if (ladderCollider instanceof RectangleCollider rectCollider) {
      final double ladderHalfHeight = rectCollider.height() / 2.0;
      final double ladderTopY = ladderPos.y() - ladderHalfHeight;
      return entityPos.y() <= ladderTopY;
    }
    return false;
  }

  private void snapToLadderCenter(Entity entity, Entity ladder) {
    PositionComponent entityPos = entity.getComponent(PositionComponent.class).orElseThrow();
    PositionComponent ladderPos = ladder.getComponent(PositionComponent.class).orElseThrow();
    entity.updateComponent(new PositionComponent(ladderPos.x(), entityPos.y()));
  }

  private void snapToLadderEdge(Entity entity, Entity ladder, VelocityComponent velocity) {
    PositionComponent entityPos = entity.getComponent(PositionComponent.class).orElseThrow();
    PositionComponent ladderPos = ladder.getComponent(PositionComponent.class).orElseThrow();
    Collider ladderCollider = ladder.getComponent(Collider.class).orElseThrow();

    if (ladderCollider instanceof RectangleCollider rectCollider) {
      final double ladderHalfWidth = rectCollider.width() / 2.0;
      final double newX =
          velocity.dx() < 0 ? ladderPos.x() - ladderHalfWidth : ladderPos.x() + ladderHalfWidth;

      entity.updateComponent(new PositionComponent(newX, entityPos.y()));
    }
  }

  private void snapToLadderTop(Entity entity, Entity ladder) {
    PositionComponent entityPos = entity.getComponent(PositionComponent.class).orElseThrow();
    PositionComponent ladderPos = ladder.getComponent(PositionComponent.class).orElseThrow();
    Collider ladderCollider = ladder.getComponent(Collider.class).orElseThrow();
    Collider entityCollider = entity.getComponent(Collider.class).orElseThrow();

    if (ladderCollider instanceof RectangleCollider rectCollider) {
      final double ladderTop = ladderPos.y() - rectCollider.height() / 2.0;
      double newY = ladderTop - entityCollider.height() / 2.0;

      entity.updateComponent(new PositionComponent(entityPos.x(), newY));
    }
  }
}
