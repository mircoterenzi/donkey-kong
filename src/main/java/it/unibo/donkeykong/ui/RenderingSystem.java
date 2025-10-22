package it.unibo.donkeykong.ui;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.Graphic;
import it.unibo.donkeykong.ecs.component.Position;
import it.unibo.donkeykong.ecs.entity.Entity;
import it.unibo.donkeykong.ecs.system.GameSystem;
import java.util.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class RenderingSystem implements GameSystem {

  private final GraphicsContext context;
  private final Map<String, Image> assetCache;

  public RenderingSystem(final Canvas canvas) {
    this.context = canvas.getGraphicsContext2D();
    this.context.setImageSmoothing(false);
    this.assetCache = new HashMap<>();
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
  public void update(World world, long deltaTime) {
    context.clearRect(0, 0, context.getCanvas().getWidth(), context.getCanvas().getHeight());
    context.drawImage(
        assetCache.get("background"),
        0,
        0,
        ConfigurationUI.WINDOW_WIDTH,
        ConfigurationUI.WINDOW_HEIGHT);
    for (final Entity entity :
        world.getEntitiesWithComponents(List.of(Position.class, Graphic.class))) {
      final Position position = entity.getComponent(Position.class).orElseThrow();
      final Graphic graphic = entity.getComponent(Graphic.class).orElseThrow();
      final Image image = assetCache.get(graphic.id());
      if (image != null) {
        context.drawImage(image, position.x(), position.y(), graphic.width(), graphic.height());
      }
    }
  }
}
