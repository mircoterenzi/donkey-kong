package it.unibo.donkeykong.ui;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.AnimationComponent;
import it.unibo.donkeykong.ecs.component.GraphicComponent;
import it.unibo.donkeykong.ecs.component.StateComponent;
import it.unibo.donkeykong.ecs.component.StateComponent.State;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import java.util.List;
import java.util.Set;

/** AnimationSystem updates the animation state (frame index and time) for entities. */
public class AnimationSystem implements GameSystem {

  @Override
  public void update(World world, float deltaTime) {
    Set<Entity> animatedEntities =
        world.getEntitiesWithComponents(List.of(GraphicComponent.class, StateComponent.class));
    for (final Entity entity : animatedEntities) {
      final GraphicComponent graphic = entity.getComponent(GraphicComponent.class).orElseThrow();
      final State state = entity.getComponent(StateComponent.class).orElseThrow().state();
      AnimationComponent animation =
          entity
              .getComponent(AnimationComponent.class)
              .orElseGet(
                  () -> {
                    AnimationComponent newAnimation = new AnimationComponent(0, state, 0);
                    entity.addComponent(newAnimation);
                    return newAnimation;
                  });
      if (animation.state() != state) {
        AnimationComponent newAnimation = new AnimationComponent(0, state, 0);
        entity.updateComponent(newAnimation);
        animation = newAnimation;
      }
      final int numberOfFrames = graphic.stateToAnimationSettings().apply(state).numberOfFrames();
      if (numberOfFrames > 1) {
        float timeSinceLastFrame = animation.timeSinceLastFrame() + deltaTime;
        if (timeSinceLastFrame >= graphic.frameDuration()) {
          final int updatedFrameIndex = (animation.frameIndex() + 1) % numberOfFrames;
          entity.updateComponent(new AnimationComponent(updatedFrameIndex, state, 0));
        } else {
          entity.updateComponent(
              new AnimationComponent(animation.frameIndex(), state, timeSinceLastFrame));
        }
      }
    }
  }
}
