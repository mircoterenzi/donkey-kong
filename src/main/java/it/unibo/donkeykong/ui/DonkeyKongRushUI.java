package it.unibo.donkeykong.ui;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.WorldImpl;
import it.unibo.donkeykong.ecs.component.Graphic;
import it.unibo.donkeykong.ecs.component.StateComponent;
import it.unibo.donkeykong.ecs.component.Position;
import it.unibo.donkeykong.utilities.InputHandler;
import java.util.List;
import java.util.Map;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import static it.unibo.donkeykong.ecs.component.StateComponent.Direction.*;
import static it.unibo.donkeykong.ecs.component.StateComponent.State.*;


public class DonkeyKongRushUI extends Application {

  /** Target frame duration in nanoseconds for 60 FPS. */
  private static final long TARGET_FPS_NANO = 1_000_000_000L / 60;

  @Override
  public void start(Stage primaryStage) {
    final World world = new WorldImpl();

    // TODO: entity creation should be handled by a dedicated class (using entity factory)
    world
        .createEntity()
        .addComponent(new Position(242, 260))
        .addComponent(new Graphic(64, 64, 100, new StateComponent(IDLE, LEFT),
          0, Map.of(IDLE, List.of("player"))));

    final Canvas canvas = new Canvas(ConfigurationUI.WINDOW_WIDTH, ConfigurationUI.WINDOW_HEIGHT);
    final Pane root = new Pane(canvas);
    final Scene scene =
        new Scene(root, ConfigurationUI.WINDOW_WIDTH, ConfigurationUI.WINDOW_HEIGHT);
    final InputHandler inputHandler = new InputHandler(world);
    // TODO: move input handling where game main scene is created
    scene.setOnKeyPressed(e -> inputHandler.handleKeyEvent(e.getCode(), true));
    scene.setOnKeyReleased(e -> inputHandler.handleKeyEvent(e.getCode(), false));

    final RenderingSystem renderingSystem = new RenderingSystem(canvas);
    world.addSystem(renderingSystem);

    new AnimationTimer() {
      private long lastUpdate = 0;

      @Override
      public void handle(long now) {
        if (now - lastUpdate >= TARGET_FPS_NANO) {
          final long deltaTime = (now - lastUpdate) / 1_000_000;
          world.update(deltaTime);
          lastUpdate = now;
        }
      }
    }.start();

    primaryStage.setTitle(ConfigurationUI.WINDOW_TITLE);
    primaryStage.setScene(scene);
    primaryStage.show();
    primaryStage.setResizable(false);
  }
}
