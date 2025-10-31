package it.unibo.donkeykong.ui;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.WorldImpl;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.system.*;
import it.unibo.donkeykong.ecs.factory.*;
import it.unibo.donkeykong.ecs.system.*;
import it.unibo.donkeykong.utilities.Constants;
import it.unibo.donkeykong.utilities.InputHandler;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class DonkeyKongRushUI extends Application {

  /** Target frame duration in nanoseconds for 60 FPS. */
  private static final long TARGET_FPS_NANO = 1_000_000_000L / 60;

  @Override
  public void start(Stage primaryStage) {
    final World world = new WorldImpl();
    world.addSystem(new MovementSystem());
    world.addSystem(new BoundariesSystem());
    world.addSystem(new CollisionSystem());
    world.addSystem(new PhysicsSystem());
    world.addSystem(new InputProcessorSystem());
    world.addSystem(new GravitySystem());
    world.addSystem(new EventDispatchSystem());

    // TODO: entity generation here? Not so sure, in dedicated controller class for mvc
    final EntityFactory entityFactory = new EntityFactoryImpl(world);
    final MapFactory mapFactory = new MapFactory(entityFactory);
    entityFactory.createFirstPlayer();
    mapFactory.generateMap();
    // TODO: entity creation should be handled by a dedicated class (using entity factory)
    world
        .createEntity()
        .addComponent(new Position(242, 500))
        .addComponent(new GroundComponent())
        .addComponent(
            new Graphic(
                "/sprites/player.png",
                250,
                16,
                1,
                2,
                0,
                (state) -> new Graphic.AnimationSettings(0, 0, 1)))
        .addComponent(new RectangleCollider(500, 32));
    world
        .createEntity()
        .addComponent(new Position(242, 200))
        .addComponent(new Input())
        .addComponent(new Gravity(Constants.GRAVITY))
        .addComponent(new Velocity(0, 0))
        .addComponent(new StateComponent(IDLE, RIGHT))
        .addComponent(
            new Graphic(
                "/sprites/mario.png",
                16,
                16,
                0.25,
                2,
                0.15f,
                (state) -> {
                  if (state == MOVING) {
                    return new Graphic.AnimationSettings(1, 0, 2);
                  }
                  return new Graphic.AnimationSettings(0, 0, 1);
                }))
        .addComponent(new CircleCollider(16));

    final double aspectRatio = Constants.WORLD_WIDTH / (double) Constants.WORLD_HEIGHT;
    final Rectangle2D screen = Screen.getPrimary().getVisualBounds();
    final double windowHeight = screen.getHeight() * 0.9;
    final double windowWidth = windowHeight * aspectRatio;

    final Canvas canvas = new Canvas(windowWidth, windowHeight);
    final Pane root = new Pane(canvas);
    final Scene scene = new Scene(root, windowWidth, windowHeight);
    final InputHandler inputHandler = new InputHandler(world);
    // TODO: move input handling where game main scene is created
    scene.setOnKeyPressed(e -> inputHandler.handleKeyEvent(e.getCode(), true));
    scene.setOnKeyReleased(e -> inputHandler.handleKeyEvent(e.getCode(), false));

    world.addSystem(new AnimationSystem());
    world.addSystem(new RenderingSystem(canvas));

    new AnimationTimer() {
      private long lastUpdate = 0;

      @Override
      public void handle(long now) {
        if (now - lastUpdate >= TARGET_FPS_NANO) {
          final float deltaTime = (now - lastUpdate) / 1_000_000_000f;
          world.update(deltaTime);
          lastUpdate = now;
        }
      }
    }.start();

    primaryStage.setTitle(ConfigurationUI.WINDOW_TITLE);
    primaryStage.setScene(scene);
    primaryStage.show();
    primaryStage.setResizable(false);
    primaryStage.toFront();
  }
}
