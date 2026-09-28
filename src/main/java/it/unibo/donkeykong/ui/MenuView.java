package it.unibo.donkeykong.ui;

import io.vertx.core.Vertx;
import it.unibo.donkeykong.network.discovery.DiscoveryClient;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class MenuView {

  private static final Logger LOG = Logger.getLogger(MenuView.class.getName());

  public static Scene create(
      Vertx vertx,
      Runnable onHostFallback,
      Consumer<String> onJoinPlay,
      Consumer<String> onJoinSpectate) {
    Button playButton = new Button("Play");
    Button spectateButton = new Button("Spectate");
    Label statusLabel = new Label();
    statusLabel.setStyle("-fx-text-fill: #555555; -fx-font-style: italic;");

    playButton.setOnAction(
        e -> {
          playButton.setDisable(true);
          spectateButton.setDisable(true);
          statusLabel.setText("Searching for a game...");

          DiscoveryClient discovery = new DiscoveryClient(vertx);
          discovery
              .discoverPlay()
              .onComplete(
                  ar ->
                      Platform.runLater(
                          () -> {
                            if (ar.succeeded()) {
                              statusLabel.setText("Game found! Connecting...");
                              onJoinPlay.accept(ar.result());
                            } else {
                              LOG.info("No game found. Starting as Host...");
                              statusLabel.setText("No game found. Starting as Host...");
                              onHostFallback.run();
                            }
                          }));
        });

    spectateButton.setOnAction(
        e -> {
          playButton.setDisable(true);
          spectateButton.setDisable(true);
          statusLabel.setText("Searching for a game to spectate...");

          DiscoveryClient discovery = new DiscoveryClient(vertx);
          discovery
              .discoverSpectate()
              .onComplete(
                  ar ->
                      Platform.runLater(
                          () -> {
                            if (ar.succeeded()) {
                              statusLabel.setText("Game found! Waiting for host to start...");
                              onJoinSpectate.accept(ar.result());
                            } else {
                              LOG.log(Level.SEVERE, "Error during spectator search", ar.cause());
                              statusLabel.setText("Error during search.");
                              playButton.setDisable(false);
                              spectateButton.setDisable(false);
                            }
                          }));
        });

    VBox menuRoot = new VBox(20, playButton, spectateButton, statusLabel);
    menuRoot.setAlignment(Pos.CENTER);
    return new Scene(menuRoot, 400, 300);
  }
}
