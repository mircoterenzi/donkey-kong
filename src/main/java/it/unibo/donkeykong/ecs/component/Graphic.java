package it.unibo.donkeykong.ecs.component;

import it.unibo.donkeykong.ecs.component.StateComponent.*;
import java.util.List;
import java.util.Map;

public record Graphic(
    double width,
    double height,
    long frameDuration,
    StateComponent state,
    int frameIndex,
    Map<State, List<String>> framesMap)
    implements Component {

  public String currentFrame() {
    return this.framesMap.get(this.state.state()).get(this.frameIndex);
  }

  public Graphic copyWithUpdatedIndex(final int frameIndex) {
    return new Graphic(
        this.width,
        this.height,
        this.frameDuration,
        this.state,
        frameIndex % this.framesMap.get(this.state.state()).size(),
        framesMap);
  }

  public Graphic copyWithUpdatedState(final StateComponent state) {
    return new Graphic(this.width, this.height, this.frameDuration, state, 0, framesMap);
  }
}
