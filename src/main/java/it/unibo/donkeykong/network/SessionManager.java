package it.unibo.donkeykong.network;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import it.unibo.donkeykong.network.client.ClientVerticle;
import it.unibo.donkeykong.network.protocol.MessageType;
import it.unibo.donkeykong.network.protocol.Net;
import it.unibo.donkeykong.network.server.LobbyVerticle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

public class SessionManager {

  private static final Logger LOG = Logger.getLogger(SessionManager.class.getName());
  private final Vertx vertx = Vertx.vertx();

  private volatile String myRole;
  private volatile String lobbyDeploymentId;
  private volatile String clientDeploymentId;

  private BiConsumer<Boolean, String> onGameStart;
  private Consumer<String> onGameOver;

  public void initializeListeners() {
    vertx
        .eventBus()
        .<JsonObject>consumer(
            Net.inbound(MessageType.ROLE_ASSIGNMENT),
            msg -> {
              this.myRole = msg.body().getString("role");
              LOG.info("Assigned role: " + myRole);
            });

    vertx
        .eventBus()
        .<JsonObject>consumer(
            Net.inbound(MessageType.GAME_START),
            msg -> {
              boolean isReconnect = msg.body().getBoolean("isReconnect", false);
              LOG.info("Game started. Reconnect: " + isReconnect);
              Platform.runLater(
                  () -> {
                    if (onGameStart != null) onGameStart.accept(isReconnect, myRole);
                  });
            });

    vertx
        .eventBus()
        .<JsonObject>consumer(
            Net.inbound(MessageType.GAME_OVER),
            msg -> {
              String winner = msg.body().getString("winner");
              LOG.info("Game over. Winner: " + winner);
              Platform.runLater(() -> stopSessionAndShowGameOver(winner));
            });

    vertx
        .eventBus()
        .<String>consumer(
            "lobby.yield",
            msg ->
                Platform.runLater(
                    () -> {
                      LOG.info("Yielding lobby to host: " + msg.body());
                      String oldClient = clientDeploymentId;
                      lobbyDeploymentId = null;
                      clientDeploymentId = null;

                      if (oldClient != null) {
                        vertx.undeploy(oldClient);
                      }
                      deployClient("/play", msg.body());
                    }));

    vertx
        .eventBus()
        .<JsonObject>consumer(
            "game.disconnected",
            msg ->
                Platform.runLater(
                    () -> {
                      String disconnectedId = msg.body().getString("deploymentId");
                      if (clientDeploymentId == null
                          || (disconnectedId != null
                              && !disconnectedId.equals(clientDeploymentId))) {
                        return;
                      }
                      LOG.warning("Disconnected from game session");
                      stopSessionAndShowGameOver("NONE");
                    }));
  }

  private void stopSessionAndShowGameOver(String winner) {
    undeployAll();
    if (onGameOver != null) {
      onGameOver.accept(winner);
    }
  }

  public void deployClient(String path, String ip) {
    vertx
        .deployVerticle(new ClientVerticle(path, ip))
        .onComplete(
            ar -> {
              if (ar.succeeded()) {
                clientDeploymentId = ar.result();
                LOG.info("Client verticle deployed successfully");
              } else {
                LOG.log(Level.SEVERE, "Failed to deploy client verticle", ar.cause());
              }
            });
  }

  public void deployHostAndClient() {
    vertx
        .deployVerticle(new LobbyVerticle())
        .onComplete(
            resLobby -> {
              if (resLobby.succeeded()) {
                lobbyDeploymentId = resLobby.result();
                LOG.info("Lobby verticle deployed successfully");
                deployClient("/play", "127.0.0.1");
              } else {
                LOG.log(Level.SEVERE, "Failed to deploy lobby verticle", resLobby.cause());
              }
            });
  }

  private void undeployAll() {
    if (clientDeploymentId != null) vertx.undeploy(clientDeploymentId);
    if (lobbyDeploymentId != null) vertx.undeploy(lobbyDeploymentId);
    clientDeploymentId = null;
    lobbyDeploymentId = null;
  }

  public void shutdown() {
    undeployAll();
    vertx.close();
  }

  public Vertx getVertx() {
    return vertx;
  }

  public String getRole() {
    return myRole;
  }

  public void setOnGameStart(BiConsumer<Boolean, String> onGameStart) {
    this.onGameStart = onGameStart;
  }

  public void setOnGameOver(Consumer<String> onGameOver) {
    this.onGameOver = onGameOver;
  }
}
