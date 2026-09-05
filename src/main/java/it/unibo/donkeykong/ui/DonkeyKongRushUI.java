package it.unibo.donkeykong.ui;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import it.unibo.donkeykong.client.network.ClientVerticle;
import it.unibo.donkeykong.core.Constants;
import it.unibo.donkeykong.core.MapFactory;
import it.unibo.donkeykong.core.WorldImpl;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.entity.EntityFactoryImpl;
import it.unibo.donkeykong.ecs.entity.api.EntityFactory;
import it.unibo.donkeykong.ecs.system.*;
import it.unibo.donkeykong.server.network.LobbyVerticle;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class DonkeyKongRushUI extends Application {

  /** Target frame duration in nanoseconds for 60 FPS. */
  private static final long TARGET_FPS_NANO = 1_000_000_000L / 60;

  public static final String WINDOW_TITLE = "Donkey Kong: Rush";

  private final Vertx vertx = Vertx.vertx();

  private String myRole;
  private AnimationTimer gameLoop;

  private String lobbyDeploymentId;
  private String clientDeploymentId;
  private boolean isEventBusSetup = false;

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle(WINDOW_TITLE);

    primaryStage.setOnCloseRequest(e -> shutdownApp());

    if (!isEventBusSetup) {
      setupNetworkListeners(primaryStage);
      isEventBusSetup = true;
    }

    showMainMenu(primaryStage);
  }

  private void setupNetworkListeners(Stage primaryStage) {
    vertx
        .eventBus()
        .<String>consumer(
            "game.role",
            msg -> {
              this.myRole = msg.body();
              System.out.println("UI: role saved " + this.myRole);
            });

    vertx
        .eventBus()
        .<JsonObject>consumer(
            "game.start",
            msg -> {
              Platform.runLater(() -> startGame(primaryStage));
              System.out.println("UI: game started");
            });

    vertx
        .eventBus()
        .<JsonObject>consumer(
            "game.over",
            msg ->
                Platform.runLater(
                    () -> {
                      String winner = msg.body().getString("winner");
                      String reason = msg.body().getString("reason");
                      System.out.println(
                          "UI: game over, winner: " + winner + ", reason: " + reason);
                      if (gameLoop != null) {
                        gameLoop.stop();
                      }
                      if (clientDeploymentId != null) vertx.undeploy(clientDeploymentId);
                      if (lobbyDeploymentId != null) vertx.undeploy(lobbyDeploymentId);

                      clientDeploymentId = null;
                      lobbyDeploymentId = null;

                      showGameOverScreen(primaryStage, winner);
                    }));
  }

  private void showMainMenu(Stage primaryStage) {
    Button playButton = new Button("Play");
    Button spectateButton = new Button("Spectate");

    playButton.setOnAction(
        e -> {
          playButton.setDisable(true);
          spectateButton.setDisable(true);
          vertx
              .deployVerticle(new LobbyVerticle())
              .onComplete(
                  ar -> {
                    if (ar.succeeded()) lobbyDeploymentId = ar.result();

                    vertx
                        .deployVerticle(new ClientVerticle("/play"))
                        .onComplete(
                            ar2 -> {
                              if (ar2.succeeded()) clientDeploymentId = ar2.result();
                            });
                  });
        });

    spectateButton.setOnAction(
        e -> {
          playButton.setDisable(true);
          spectateButton.setDisable(true);
          vertx
              .deployVerticle(new ClientVerticle("/spectate"))
              .onComplete(
                  ar -> {
                    if (ar.succeeded()) clientDeploymentId = ar.result();
                  });
        });

    VBox menuRoot = new VBox(20, playButton, spectateButton);
    menuRoot.setAlignment(Pos.CENTER);

    Scene menuScene = new Scene(menuRoot, 400, 300);
    primaryStage.setScene(menuScene);
    primaryStage.setResizable(false);
    primaryStage.show();
    primaryStage.centerOnScreen();
  }

  private void showGameOverScreen(Stage primaryStage, String winner) {
    String resultText;
    if ("SPECTATOR".equals(myRole)) {
      resultText = "Game over, winner: " + winner;
    } else if (winner.equals(myRole)) {
      resultText = "You win!";
    } else {
      resultText = "You lost!";
    }

    VBox root = getRoot(primaryStage, resultText);

    Scene gameOverScene = new Scene(root, 400, 300);
    primaryStage.setScene(gameOverScene);
    primaryStage.centerOnScreen();
  }

  private VBox getRoot(Stage primaryStage, String resultText) {
    Label titleLabel = new Label(resultText);
    titleLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");

    Button lobbyButton = new Button("Back to Lobby");
    lobbyButton.setOnAction(e -> showMainMenu(primaryStage));

    Button exitButton = new Button("Exit Game");
    exitButton.setOnAction(e -> shutdownApp());

    VBox root = new VBox(20, titleLabel, lobbyButton, exitButton);
    root.setAlignment(Pos.CENTER);
    return root;
  }

  private void shutdownApp() {
    if (gameLoop != null) {
      gameLoop.stop();
    }
    vertx
        .close()
        .onComplete(
            v -> {
              Platform.exit();
              System.exit(0);
            });
  }

  private void startGame(Stage primaryStage) {
    final World world = new WorldImpl();

    this.gameLoop =
        new AnimationTimer() {
          private long lastUpdate = 0;

          @Override
          public void handle(long now) {
            if (lastUpdate == 0) {
              lastUpdate = now;
              return;
            }
            if (now - lastUpdate >= TARGET_FPS_NANO) {
              final float deltaTime = (now - lastUpdate) / 1_000_000_000f;
              world.update(deltaTime);
              lastUpdate = now;
            }
          }
        };

    final EntityFactory entityFactory = new EntityFactoryImpl(world, myRole);
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
    world.addSystem(
        new HealthSystem(
            deadEntity -> {
              JsonObject deathMsg = new JsonObject().put("type", "PLAYER_DIED");
              vertx.eventBus().send("outbound.messages", deathMsg);
              System.out.println("UI: Player " + deadEntity.getId() + " has died!");
            }));
    if ("HOST".equals(myRole)) {
      world.addSystem(new SpawnSystem(entityFactory));
    }
    world.addSystem(new ClimbingSystem());
    world.addSystem(new InputSystem());
    world.addSystem(new GravitySystem());
    world.addSystem(new StateReceiverSystem(vertx.eventBus(), myRole, entityFactory));
    world.addSystem(
        new WinSystem(
            winner -> {
              JsonObject goalMsg = new JsonObject().put("type", "GOAL_REACHED");
              vertx.eventBus().send("outbound.messages", goalMsg);
              System.out.println("UI: Player " + winner + " has reached the goal!");
            }));
    world.addSystem(new EventDispatchSystem());
    world.addSystem(new NetworkBroadcastSystem(vertx.eventBus(), myRole));

    final double aspectRatio = Constants.WORLD_WIDTH / (double) Constants.WORLD_HEIGHT;
    final Rectangle2D screen = Screen.getPrimary().getVisualBounds();
    final double windowHeight = screen.getHeight() * 0.9;
    final double windowWidth = windowHeight * aspectRatio;

    final Canvas canvas = new Canvas(windowWidth, windowHeight);
    final Pane root = new Pane(canvas);
    final Scene scene = new Scene(root, windowWidth, windowHeight);

    final InputHandler inputHandler = new InputHandler(world, myRole);
    scene.setOnKeyPressed(e -> inputHandler.handleKeyEvent(e.getCode(), true));
    scene.setOnKeyReleased(e -> inputHandler.handleKeyEvent(e.getCode(), false));

    world.addSystem(new AnimationSystem());
    world.addSystem(new RenderingSystem(canvas));

    gameLoop.start();

    primaryStage.setScene(scene);
    primaryStage.centerOnScreen();
  }
}
