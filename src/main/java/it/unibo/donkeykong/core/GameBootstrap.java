package it.unibo.donkeykong.core;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.NetworkComponent;
import it.unibo.donkeykong.ecs.entity.EntityFactoryImpl;
import it.unibo.donkeykong.ecs.entity.api.EntityFactory;
import it.unibo.donkeykong.ecs.system.*;
import it.unibo.donkeykong.network.protocol.Net;
import it.unibo.donkeykong.ui.AnimationSystem;
import it.unibo.donkeykong.ui.InputHandler;
import it.unibo.donkeykong.ui.RenderingSystem;
import javafx.animation.AnimationTimer;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;

public class GameBootstrap {

  private static final long TARGET_FPS_NANO = 1_000_000_000L / 60;
  private static final long RECONNECT_TIME_OFFSET_MS = 4000;

  private AnimationTimer gameLoop;
  private StateReceiverSystem stateReceiverSystem;

  public Scene buildGameScene(Vertx vertx, String myRole, boolean isReconnect) {
    final World world = new WorldImpl();

    boolean skipCountdown = isReconnect || "SPECTATOR".equals(myRole);
    long gameStartTime =
        System.currentTimeMillis() - (skipCountdown ? RECONNECT_TIME_OFFSET_MS : 0);

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
            deadEntity ->
                vertx.eventBus().send(Net.OUTBOUND, new JsonObject().put("type", "PLAYER_DIED")),
            destroyedEntity ->
                destroyedEntity
                    .getComponent(NetworkComponent.class)
                    .ifPresent(
                        net -> {
                          JsonObject msg =
                              new JsonObject()
                                  .put("type", "ENTITY_DESTROYED")
                                  .put("id", net.networkId());
                          vertx.eventBus().send(Net.OUTBOUND, msg);
                        })));

    if ("HOST".equals(myRole)) {
      world.addSystem(new SpawnSystem(entityFactory));
    }

    world.addSystem(new ClimbingSystem());
    world.addSystem(new InputSystem(gameStartTime));
    world.addSystem(new GravitySystem());

    this.stateReceiverSystem = new StateReceiverSystem(vertx.eventBus(), myRole, entityFactory);
    world.addSystem(stateReceiverSystem);

    world.addSystem(
        new WinSystem(
            winner -> {
              JsonObject goalMsg = new JsonObject().put("type", "GOAL_REACHED");
              vertx.eventBus().send(Net.OUTBOUND, goalMsg);
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
    world.addSystem(new RenderingSystem(canvas, gameStartTime));

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

    return scene;
  }

  public void startLoop() {
    if (gameLoop != null) gameLoop.start();
  }

  public void stop() {
    if (stateReceiverSystem != null) stateReceiverSystem.stop();
    if (gameLoop != null) gameLoop.stop();
  }
}
