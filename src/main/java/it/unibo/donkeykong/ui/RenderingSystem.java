package it.unibo.donkeykong.ui;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.Graphic;
import it.unibo.donkeykong.ecs.component.Position;
import it.unibo.donkeykong.ecs.entity.Entity;
import it.unibo.donkeykong.ecs.system.GameSystem;
import it.unibo.donkeykong.utilities.Constants;
import java.util.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class RenderingSystem implements GameSystem {

  private final GraphicsContext context;
  private final Map<String, Image> assetCache;
  private final double scaleX;
  private final double scaleY;

  public RenderingSystem(final Canvas canvas) {
    this.context = canvas.getGraphicsContext2D();
    this.context.setImageSmoothing(false);
    this.assetCache = new HashMap<>();
    this.scaleX = canvas.getWidth() / Constants.WORLD_WIDTH;
    this.scaleY = canvas.getHeight() / Constants.WORLD_HEIGHT;
    this.loadAssets();
  }

  private void loadAssets() {
    ConfigurationUI.ASSET_PATHS.forEach(
        (id, path) -> {
          try {
            this.assetCache.put(
                id, new Image(Objects.requireNonNull(getClass().getResourceAsStream(path))));
          } catch (Exception e) {
            System.err.println("Failed to load asset: " + path + ". Check if the file exists.");
          }
        });
  }

  @Override
  public void update(World world, float deltaTime) {
    context.save();
    context.scale(scaleX, scaleY);

    context.clearRect(0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
    context.drawImage(
        assetCache.get("background"), 0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
    for (final Entity entity :
        world.getEntitiesWithComponents(List.of(Position.class, Graphic.class))) {
      final Position position = entity.getComponent(Position.class).orElseThrow();
      final Graphic graphic = entity.getComponent(Graphic.class).orElseThrow();
      final Image image = assetCache.get(graphic.currentFrame());
      double renderPositionY = position.y() - graphic.height() / 2;
      double renderPositionX = position.x() - graphic.width() / 2;
      if (image != null) {
        context.drawImage(
            image, renderPositionX, renderPositionY, graphic.width(), graphic.height());
      } else {
        context.setStroke(javafx.scene.paint.Color.GREEN);
        context.setLineWidth(2);
        context.strokeRect(renderPositionX, renderPositionY, graphic.width(), graphic.height());
        context.setFill(javafx.scene.paint.Color.BLUE);
        context.fillRect(renderPositionX, renderPositionY, graphic.width(), graphic.height());
      }
    }

    context.restore();
  }
}
