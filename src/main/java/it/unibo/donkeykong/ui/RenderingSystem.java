package it.unibo.donkeykong.ui;

import it.unibo.donkeykong.core.Constants;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.StateComponent.State;
import it.unibo.donkeykong.ecs.component.api.Collider;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.system.api.GameSystem;
import java.util.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class RenderingSystem implements GameSystem {

  private final GraphicsContext context;
  private final Map<String, Map<StateComponent.State, List<Image>>> assetCache;
  private final double scaleX;
  private final double scaleY;
  private final Image backgroundImage;

  public RenderingSystem(final Canvas canvas) {
    this.context = canvas.getGraphicsContext2D();
    this.context.setImageSmoothing(false);
    this.assetCache = new HashMap<>();
    this.scaleX = canvas.getWidth() / Constants.WORLD_WIDTH;
    this.scaleY = canvas.getHeight() / Constants.WORLD_HEIGHT;
    this.backgroundImage =
        new Image(
            Objects.requireNonNull(getClass().getResourceAsStream("/images/world-background.png")));
  }

  private void sliceSpriteSheetFrames(
      GraphicComponent graphic, State state, Map<State, List<Image>> stateMap) {
    List<Image> frames = new ArrayList<>();
    final GraphicComponent.AnimationSettings settings =
        graphic.stateToAnimationSettings().apply(state);
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

  private void drawFallbackShapeBasedOnCollision(double x, double y, Collider collider) {
    if (collider instanceof CircleCollider circle) {
      context.setFill(Color.GREEN);
      context.fillOval(x, y, circle.radius() * 2, circle.radius() * 2);
    } else if (collider instanceof RectangleCollider rectangle) {
      context.setFill(Color.GREEN);
      context.fillRect(x, y, rectangle.width(), rectangle.height());
    }
  }

  @Override
  public void update(World world, float deltaTime) {
    context.save();
    context.scale(scaleX, scaleY);
    context.clearRect(0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
    context.drawImage(this.backgroundImage, 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
    for (final Entity entity :
        world.getEntitiesWithComponents(List.of(PositionComponent.class, GraphicComponent.class))) {
      final PositionComponent position = entity.getComponent(PositionComponent.class).orElseThrow();
      final GraphicComponent graphic = entity.getComponent(GraphicComponent.class).orElseThrow();
      final Optional<AnimationComponent> optAnimation =
          entity.getComponent(AnimationComponent.class);
      final Optional<StateComponent> optState = entity.getComponent(StateComponent.class);
      final double renderPositionY = position.y() - (graphic.scaledHeight() / 2);
      final double renderPositionX = position.x() - (graphic.scaledWidth() / 2);
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
          if (optState.get().direction().equals(StateComponent.Direction.LEFT)) {
            context.save();
            context.translate(renderPositionX + graphic.scaledWidth(), 0);
            context.scale(-1, 1);
            context.drawImage(
                image, 0, renderPositionY, graphic.scaledWidth(), graphic.scaledHeight());
            context.restore();
          } else {
            context.drawImage(
                image,
                renderPositionX,
                renderPositionY,
                graphic.scaledWidth(),
                graphic.scaledHeight());
          }
        } else {
          entity
              .getComponent(Collider.class)
              .ifPresent(
                  collider ->
                      drawFallbackShapeBasedOnCollision(
                          renderPositionX, renderPositionY, collider));
        }
      } else {
        entity
            .getComponent(Collider.class)
            .ifPresent(
                collider ->
                    drawFallbackShapeBasedOnCollision(renderPositionX, renderPositionY, collider));
      }
    }

    context.setLineWidth(1);

    context.setStroke(javafx.scene.paint.Color.RED);
    for (final Entity entity :
        world.getEntitiesWithComponents(
            List.of(PositionComponent.class, RectangleCollider.class, SolidComponent.class))) {

      final PositionComponent pos = entity.getComponent(PositionComponent.class).orElseThrow();
      final RectangleCollider coll = entity.getComponent(RectangleCollider.class).orElseThrow();

      // Assumendo che PositionComponent sia il centro, come per le entità con GraphicComponent
      final double x = pos.x() - (double) coll.width() / 2;
      final double y = pos.y() - (double) coll.height() / 2;

      context.strokeRect(x, y, coll.width(), coll.height());
    }

    context.setStroke(javafx.scene.paint.Color.BLUE);
    for (final Entity entity :
        world.getEntitiesWithComponents(
            List.of(PositionComponent.class, RectangleCollider.class, ClimbableComponent.class))) {

      final PositionComponent pos = entity.getComponent(PositionComponent.class).orElseThrow();
      final RectangleCollider coll = entity.getComponent(RectangleCollider.class).orElseThrow();

      // Assumendo che PositionComponent sia il centro
      final double x = pos.x() - (double) coll.width() / 2;
      final double y = pos.y() - (double) coll.height() / 2;

      context.strokeRect(x, y, coll.width(), coll.height());
    }

    context.restore();
  }
}
