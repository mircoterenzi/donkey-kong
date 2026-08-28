package it.unibo.donkeykong.ui;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.parsetools.RecordParser;
import it.unibo.donkeykong.core.Constants;
import it.unibo.donkeykong.core.MapFactory;
import it.unibo.donkeykong.core.WorldImpl;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.entity.EntityFactoryImpl;
import it.unibo.donkeykong.ecs.entity.api.EntityFactory;
import it.unibo.donkeykong.ecs.system.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class DonkeyKongRushUI extends Application {

  /** Target frame duration in nanoseconds for 60 FPS. */
  private static final long TARGET_FPS_NANO = 1_000_000_000L / 60;

  public static final String WINDOW_TITLE = "Donkey Kong: Rush";
  private final Vertx vertx = Vertx.vertx();

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle(WINDOW_TITLE);

    Button hostButton = new Button("Host Game");
    Button joinButton = new Button("Join Game");

    hostButton.setOnAction(
        e -> {
          startServer();
          startClient("HOST group");
          // startGame(primaryStage); TODO: remove comment
        });

    joinButton.setOnAction(
        e -> {
          startClient("JOIN group");
          // startGame(primaryStage);
        });

    VBox menuRoot = new VBox(20, hostButton, joinButton);
    menuRoot.setAlignment(Pos.CENTER);

    Scene menuScene = new Scene(menuRoot, 400, 300);
    primaryStage.setScene(menuScene);
    primaryStage.setResizable(false);
    primaryStage.show();
    primaryStage.toFront();
    primaryStage.requestFocus();
  }

  private void startServer() {
    NetServer server = vertx.createNetServer();
    server.connectHandler(
        socket -> {
          System.out.println("Client connected: " + socket.remoteAddress());
          RecordParser parser = RecordParser.newDelimited("\n", socket);
          parser.handler(
              buffer -> System.out.println("Server received: " + buffer.toString().trim()));
        });
    server.listen(
        1234,
        "localhost",
        res -> {
          if (res.succeeded()) System.out.println("Server listening on 1234");
        });
  }

  private void startClient(String initialCommand) {
    NetClient client = vertx.createNetClient();
    client.connect(
        1234,
        "localhost",
        res -> {
          if (res.succeeded()) {
            System.out.println("Client connected.");
            res.result().write(initialCommand + "\n");
            RecordParser parser = RecordParser.newDelimited("\n", res.result());
            parser.handler(buffer -> System.out.println("Client received: " + buffer.toString()));
          }
        });
  }

  private void startGame(Stage primaryStage) {
    final World world = new WorldImpl();

    // TODO: entity generation here? Not so sure, in dedicated controller class for mvc
    final EntityFactory entityFactory = new EntityFactoryImpl(world);
    final MapFactory mapFactory = new MapFactory(entityFactory);

    entityFactory.createFirstPlayer();
    entityFactory.createSecondPlayer();
    entityFactory.createDonkeyKong();
    entityFactory.createPauline();
    mapFactory.generateMap();

    world.addSystem(new MovementSystem());
    world.addSystem(new BoundariesSystem());
    world.addSystem(new CollisionSystem());
    world.addSystem(new PhysicsSystem());
    world.addSystem(new HealthSystem());
    world.addSystem(new SpawnSystem(entityFactory));
    world.addSystem(new ClimbingSystem());
    world.addSystem(new InputSystem());
    world.addSystem(new GravitySystem());
    world.addSystem(new EventDispatchSystem());

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

    primaryStage.setScene(scene);
    primaryStage.centerOnScreen();
  }
}
