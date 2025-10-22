package it.unibo.donkeykong.ui;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.WorldImpl;
import it.unibo.donkeykong.ecs.component.Position;
import it.unibo.donkeykong.ecs.entity.EntityFactory;
import it.unibo.donkeykong.ecs.entity.EntityFactoryImpl;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class DonkeyKongRushUI extends Application {

  @Override
  public void start(Stage primaryStage) {
    final World world = new WorldImpl();
    final EntityFactory factory = new EntityFactoryImpl(world);
    factory.createFirstPlayer().addComponent(new Position(242, 260));

    final Canvas canvas = new Canvas(ConfigurationUI.WINDOW_WIDTH, ConfigurationUI.WINDOW_HEIGHT);
    final Pane root = new Pane(canvas);
    final Scene scene =
        new Scene(root, ConfigurationUI.WINDOW_WIDTH, ConfigurationUI.WINDOW_HEIGHT);

    final RenderingSystem renderingSystem = new RenderingSystem(canvas);
    world.addSystem(renderingSystem);

    new AnimationTimer() {
      private long lastUpdate = 0;

      @Override
      public void handle(long now) {
        if (lastUpdate > 0) {
          final long deltaTime = (now - lastUpdate) / 1_000_000;
          world.update(deltaTime);
        }
        lastUpdate = now;
      }
    }.start();

    primaryStage.setTitle(ConfigurationUI.WINDOW_TITLE);
    primaryStage.setScene(scene);
    primaryStage.show();
    primaryStage.setResizable(false);
  }
}
