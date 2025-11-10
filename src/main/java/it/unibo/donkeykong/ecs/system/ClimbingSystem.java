package it.unibo.donkeykong.ecs.system;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.StateComponent.State;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import it.unibo.donkeykong.ecs.system.common.CollisionUtils;
import java.util.List;
import java.util.Optional;

public class ClimbingSystem implements GameSystem {
  @Override
  public void update(World world, float deltaTime) {
    world
        .getEntitiesWithComponents(List.of(CollisionEventComponent.class))
        .forEach(
            entity -> {
              CollisionEventComponent collisionEvent =
                  entity.getComponent(CollisionEventComponent.class).orElseThrow();
              Optional<Entity> ladder =
                  collisionEvent.getCollisionsWith(ClimbableComponent.class).stream().findFirst();
              StateComponent oldState = entity.getComponent(StateComponent.class).orElseThrow();
              boolean canClimb =
                  CollisionUtils.areAligned(entity, ladder)
                      && oldState.state() != State.JUMP
                      && oldState.state() != State.FALL
                      && oldState.state() != State.FAST_FALL;
              double verticalVelocity =
                  entity
                      .getComponent(GravityComponent.class)
                      .map(GravityComponent::gravity)
                      .orElse(0.0);
              if (canClimb) {
                entity
                    .getComponent(VelocityComponent.class)
                    .ifPresent(
                        velocity ->
                            entity.updateComponent(
                                new VelocityComponent(velocity.dx(), -verticalVelocity)));
              }
            });
  }
}
