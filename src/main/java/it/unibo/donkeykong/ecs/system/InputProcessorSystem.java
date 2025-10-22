package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;

import java.util.List;

import static it.unibo.donkeykong.utilities.Constants.*;

/** System that processes player input and updates entity velocities accordingly. */
public class InputProcessorSystem implements GameSystem {

  @Override
  public void update(World world, long deltaTime) {
    world
        .getEntitiesWithComponents(List.of(Input.class, Velocity.class))
        .forEach(
            entity -> {
              Input input = entity.getComponent(Input.class).orElseThrow();
              Velocity oldVelocity = entity.getComponent(Velocity.class).orElseThrow();

              double newDx, newDy;
              final boolean isClimbing = world.getEntitiesWithComponents(List.of(Climbable.class)).stream()
                .anyMatch(e -> e.getComponent(CollisionEvent.class).orElseThrow().otherEntity().equals(entity));
              final boolean isGrounded = world.getEntitiesWithComponents(List.of(Position.class)).stream()
                .filter(e -> !e.equals(entity))
                .anyMatch(e -> e.getComponent(CollisionEvent.class).orElseThrow().otherEntity().equals(entity));

              newDx =
                  switch (input.getCurrentHInput()) {
                    case MOVE_LEFT -> -DX_PLAYER_VELOCITY;
                    case MOVE_RIGHT -> DX_PLAYER_VELOCITY;
                    default -> 0;
                  };

              if (isClimbing && !input.isJumpPressed()) {
                newDy =
                    switch (input.getCurrentVInput()) {
                      case MOVE_UP -> -DY_PLAYER_VELOCITY;
                      case MOVE_DOWN -> DY_PLAYER_VELOCITY;
                      default -> 0;
                    };
              } else if (input.isJumpPressed() && (isGrounded || isClimbing)) {
                newDy = -DY_PLAYER_VELOCITY * 2;
                input.setJumpPressed(false);
              } else if (input.getCurrentVInput() == Input.VerticalInput.MOVE_DOWN && !isGrounded) {
                newDy = DY_PLAYER_VELOCITY * GRAVITY;
              } else {
                newDy = 0;
              }

              entity.updateComponent(oldVelocity, new Velocity(newDx, newDy));
            });
  }
}
