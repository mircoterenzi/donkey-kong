package it.unibo.donkeykong.ecs.component;

import it.unibo.donkeykong.ecs.component.StateComponent.*;
import it.unibo.donkeykong.ecs.component.api.Component;
import java.util.function.Function;

public record Graphic(
    String path,
    double width,
    double height,
    double border,
    double scale,
    float frameDuration,
    Function<State, AnimationSettings> stateToAnimationSettings)
    implements Component {

  public record AnimationSettings(int x, int y, int numberOfFrames) {}

  public double scaledWidth() {
    return this.width * this.scale;
  }

  public double scaledHeight() {
    return this.height * this.scale;
  }
}
