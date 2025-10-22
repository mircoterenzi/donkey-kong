package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import java.util.List;

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

              newDx =
                  switch (input.getCurrentHInput()) {
                    case MOVE_LEFT -> -oldVelocity.dx();
                    case MOVE_RIGHT -> oldVelocity.dx();
                    default -> 0;
                  };

              if (isClimbing) {
                newDy =
                    switch (input.getCurrentVInput()) {
                      case MOVE_UP -> -oldVelocity.dy();
                      case MOVE_DOWN -> oldVelocity.dy();
                      default -> 0;
                    };
              } else if (input.isJumpPressed()) {
                newDy = -oldVelocity.dy() * 2;
                input.setJumpPressed(false);
              } else {
                newDy = 0;
              }

              entity.updateComponent(oldVelocity, new Velocity(newDx, newDy));
            });
  }
}
