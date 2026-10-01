package it.unibo.donkeykong.ui;

import it.unibo.donkeykong.core.GameBootstrap;
import it.unibo.donkeykong.network.SessionManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DonkeyKongRushUI extends Application {

  public static final String WINDOW_TITLE = "Donkey Kong: Rush";

  private SessionManager sessionManager;
  private GameBootstrap activeGame;

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle(WINDOW_TITLE);
    primaryStage.setOnCloseRequest(e -> shutdownApp());

    sessionManager = new SessionManager();
    sessionManager.initializeListeners();

    sessionManager.setOnGameStart(
        (isReconnect, role) -> {
          activeGame = new GameBootstrap();
          Scene gameScene = activeGame.buildGameScene(sessionManager.getVertx(), role, isReconnect);
          primaryStage.setScene(gameScene);
          primaryStage.centerOnScreen();
          activeGame.startLoop();
        });

    sessionManager.setOnGameOver(
        winner -> {
          if (activeGame != null) {
            activeGame.stop();
            activeGame = null;
          }
          showGameOverScreen(primaryStage, winner);
        });

    showMainMenu(primaryStage);
  }

  private void showMainMenu(Stage primaryStage) {
    Scene menuScene =
        MenuView.create(
            sessionManager.getVertx(),
            () -> sessionManager.deployHostAndClient(),
            ip -> sessionManager.deployClient("/play", ip),
            ip -> sessionManager.deployClient("/spectate", ip));

    primaryStage.setScene(menuScene);
    primaryStage.setResizable(false);
    primaryStage.show();
    primaryStage.centerOnScreen();
  }

  private void showGameOverScreen(Stage primaryStage, String winner) {
    Scene gameOverScene =
        GameOverView.create(
            winner, sessionManager.getRole(), () -> showMainMenu(primaryStage), this::shutdownApp);

    primaryStage.setScene(gameOverScene);
    primaryStage.centerOnScreen();
  }

  private void shutdownApp() {
    if (activeGame != null) {
      activeGame.stop();
    }
    if (sessionManager != null) {
      sessionManager.shutdown();
    }
    Platform.exit();
    System.exit(0);
  }
}
