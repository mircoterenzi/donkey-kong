package it.unibo.donkeykong.ecs.component;

import it.unibo.donkeykong.ecs.component.StateComponent.*;
import java.util.function.Function;

public record Graphic(
    String path,
    double width,
    double height,
    double border,
    float frameDuration,
    Function<State, AnimationSettings> stateToAnimationSettings)
    implements Component {

  public record AnimationSettings(int x, int y, int numberOfFrames) {}
}
