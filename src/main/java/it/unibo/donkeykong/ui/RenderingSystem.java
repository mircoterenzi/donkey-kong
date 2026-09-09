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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class RenderingSystem implements GameSystem {

  private final GraphicsContext context;
  private final Map<String, Map<StateComponent.State, List<Image>>> assetCache;
  private final double scaleX;
  private final double scaleY;
  private final Image backgroundImage;
  private final Map<String, Image> sourceImageCache = new HashMap<>();
  private final long startTime;

  public RenderingSystem(final Canvas canvas, final long startTime) {
    this.context = canvas.getGraphicsContext2D();
    this.context.setImageSmoothing(false);
    this.assetCache = new HashMap<>();
    this.scaleX = canvas.getWidth() / Constants.WORLD_WIDTH;
    this.scaleY = canvas.getHeight() / Constants.WORLD_HEIGHT;
    this.backgroundImage =
        new Image(
            Objects.requireNonNull(getClass().getResourceAsStream("/images/world-background.png")));
    this.startTime = startTime;
  }

  private void sliceSpriteSheetFrames(
      GraphicComponent graphic, State state, Map<State, List<Image>> stateMap) {
    List<Image> frames = new ArrayList<>();
    final GraphicComponent.AnimationSettings settings =
        graphic.stateToAnimationSettings().apply(state);
    final Image sourceImage;
    try {
      sourceImage =
          this.sourceImageCache.computeIfAbsent(
              graphic.path(),
              path ->
                  new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm()));
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
    context.restore();

    context.save();
    context.setFill(Color.WHITE);
    context.setFont(Font.font("Courier New", FontWeight.BOLD, 20));

    int marioLives = 3;
    int luigiLives = 3;

    for (Entity e :
        world.getEntitiesWithComponents(List.of(NetworkComponent.class, HealthComponent.class))) {
      NetworkComponent net = e.getComponent(NetworkComponent.class).orElseThrow();
      HealthComponent health = e.getComponent(HealthComponent.class).orElseThrow();
      if ("HOST".equals(net.entityType())) {
        marioLives = health.livesCount();
      } else if ("GUEST".equals(net.entityType())) {
        luigiLives = health.livesCount();
      }
    }

    context.fillText("Mario lives: " + marioLives, 30, 40);
    context.fillText("Luigi lives: " + luigiLives, 30, 70);
    context.restore();

    long elapsed = System.currentTimeMillis() - this.startTime;
    if (elapsed < 4000) {
      context.save();
      context.setFill(Color.web("#FFCC00"));
      context.setStroke(Color.web("#CC0000"));
      context.setLineWidth(4);

      Font arcadeFont =
          Font.loadFont(getClass().getResourceAsStream("/fonts/PressStart2P.ttf"), 120);
      if (arcadeFont != null) {
        context.setFont(arcadeFont);
      } else {
        context.setFont(Font.font("Consolas", FontWeight.BOLD, 120));
      }

      String text =
          elapsed < Constants.INPUT_DELAY ? String.valueOf(3 - (elapsed / 1000)) : "START";
      context.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
      context.setTextBaseline(javafx.geometry.VPos.CENTER);
      double renderX = context.getCanvas().getWidth() / 2;
      double renderY = context.getCanvas().getHeight() / 2;

      context.strokeText(text, renderX, renderY);
      context.fillText(text, renderX, renderY);
      context.restore();
    }
  }
}
