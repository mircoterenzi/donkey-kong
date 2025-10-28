package it.unibo.donkeykong.ui;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.AnimationComponent;
import it.unibo.donkeykong.ecs.component.Graphic;
import it.unibo.donkeykong.ecs.component.Position;
import it.unibo.donkeykong.ecs.component.StateComponent;
import it.unibo.donkeykong.ecs.component.StateComponent.State;
import it.unibo.donkeykong.ecs.entity.Entity;
import it.unibo.donkeykong.ecs.system.GameSystem;
import it.unibo.donkeykong.utilities.Constants;
import java.util.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class RenderingSystem implements GameSystem {

  private final GraphicsContext context;
  private final Map<String, Map<StateComponent.State, List<Image>>> assetCache;
  private final double scaleX;
  private final double scaleY;

  public RenderingSystem(final Canvas canvas) {
    this.context = canvas.getGraphicsContext2D();
    this.context.setImageSmoothing(false);
    this.assetCache = new HashMap<>();
    this.scaleX = canvas.getWidth() / Constants.WORLD_WIDTH;
    this.scaleY = canvas.getHeight() / Constants.WORLD_HEIGHT;
  }

  private void sliceSpriteSheetFrames(
      Graphic graphic, State state, Map<State, List<Image>> stateMap) {
    List<Image> frames = new ArrayList<>();
    final Graphic.AnimationSettings settings = graphic.stateToAnimationSettings().apply(state);
    final Image sourceImage;
    try {
      sourceImage =
          new Image(
              Objects.requireNonNull(getClass().getResource(graphic.path())).toExternalForm());
    } catch (Exception e) {
      System.err.println("Failed to load source image for path: " + graphic.path());
      stateMap.put(state, Collections.emptyList());
      return;
    }
    final int tileWidth = (int) graphic.width();
    final int tileHeight = (int) graphic.height();
    final int frameY = settings.y() * (int) (tileHeight + graphic.border());
    for (int i = 0; i < settings.numberOfFrames(); i++) {
      try {
        final int frameX =
            (int)
                (((settings.x() + i) * tileWidth)
                    + (settings.x() + i + 1) * (graphic.border() + 1));

        final Image subImage =
            new WritableImage(sourceImage.getPixelReader(), frameX, frameY, tileWidth, tileHeight);

        frames.add(subImage);
      } catch (Exception e) {
        System.err.println(
            "Failed to slice image for path: "
                + graphic.path()
                + " state: "
                + state
                + " frame: "
                + i);
      }
    }
    stateMap.put(state, frames);
  }

  private void drawFallbackRectangle(double x, double y, double width, double height) {
    context.setStroke(javafx.scene.paint.Color.BLACK);
    context.setLineWidth(2);
    context.strokeRect(x, y, width, height);
    context.setFill(javafx.scene.paint.Color.GREEN);
    context.fillRect(x, y, width, height);
  }

  @Override
  public void update(World world, float deltaTime) {
    context.save();
    context.scale(scaleX, scaleY);
    context.clearRect(0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
    for (final Entity entity :
        world.getEntitiesWithComponents(List.of(Position.class, Graphic.class))) {
      final Position position = entity.getComponent(Position.class).orElseThrow();
      final Graphic graphic = entity.getComponent(Graphic.class).orElseThrow();
      final Optional<AnimationComponent> optAnimation =
          entity.getComponent(AnimationComponent.class);
      final Optional<StateComponent> optState = entity.getComponent(StateComponent.class);
      final double renderPositionY = position.y() - graphic.height();
      final double renderPositionX = position.x() - graphic.width();
      if (optAnimation.isPresent() && optState.isPresent()) {
        final AnimationComponent animation = optAnimation.get();
        final State state = optState.get().state();
        if (!assetCache.containsKey(graphic.path())) {
          assetCache.put(graphic.path(), new HashMap<>());
        }
        final Map<State, List<Image>> stateMap = assetCache.get(graphic.path());
        if (!stateMap.containsKey(state)) {
          sliceSpriteSheetFrames(graphic, state, stateMap);
        }
        final List<Image> frames = stateMap.get(state);
        if (!frames.isEmpty()
            && frames.size() > animation.frameIndex()
            && frames.get(animation.frameIndex()) != null) {
          final Image image = frames.get(animation.frameIndex());
          context.drawImage(
              image, renderPositionX, renderPositionY, graphic.width() * 2, graphic.height() * 2);
          // TODO: manage reflection
        } else {
          drawFallbackRectangle(
              renderPositionX, renderPositionY, graphic.width() * 2, graphic.height() * 2);
        }
      } else {
        drawFallbackRectangle(
            renderPositionX, renderPositionY, graphic.width() * 2, graphic.height() * 2);
      }
    }
    context.restore();
  }
}
