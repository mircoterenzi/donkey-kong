package it.unibo.donkeykong.ecs.component;

import java.util.List;
import java.util.Map;

public record Graphic(
    double width,
    double height,
    long frameDuration,
    EntityState state,
    int frameIndex,
    Map<EntityState, List<String>> framesMap)
    implements Component {

  public enum EntityState {
    IDLE,
    MOVING_LEFT,
    MOVING_RIGHT,
    JUMPING,
    FALLING,
    CLIMBING,
    DYING
  }

  public String currentFrame() {
    return this.framesMap.get(this.state).get(this.frameIndex);
  }

  public Graphic copyWithUpdatedIndex(final int frameIndex) {
    return new Graphic(
        this.width,
        this.height,
        this.frameDuration,
        this.state,
        frameIndex % this.framesMap.get(this.state).size(),
        framesMap);
  }

  public Graphic copyWithUpdatedState(final EntityState state) {
    return new Graphic(this.width, this.height, this.frameDuration, state, 0, framesMap);
  }
}
