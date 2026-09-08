package it.unibo.donkeykong.network.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import java.util.*;

/**
 * LobbyVerticle is a Vert.x verticle that manages the game lobby for a multiplayer game. It handles
 * WebSocket connections and matchmaking logics, then starts and manages the game. Finally, it
 * manages handles disconnections, notifying the remaining player of the game over condition and
 * cleaning up the lobby for new connections.
 */
public class LobbyVerticle extends AbstractVerticle {

  private ServerWebSocket hostSocket;
  private ServerWebSocket guestSocket;
  private final List<ServerWebSocket> spectators = new ArrayList<>();
  private boolean gameStarted = false;
  private long guestReconnectTimerId = -1;

  /**
   * Starts the LobbyVerticle by creating an HTTP server that listens for WebSocket connections,
   * then assigns roles to the connected clients (host, guest, or spectator) and manages the game
   * state accordingly.
   */
  @Override
  public void start() {
    vertx
        .createHttpServer()
        .webSocketHandler(
            ws -> {
              if ("/spectate".equals(ws.path())) {
                spectators.add(ws);
                setupSocket(ws, "SPECTATOR");
                sendRole(ws, "SPECTATOR");
                System.out.println("Spectator connected, total spectators: " + spectators.size());
                if (gameStarted) {
                  ws.writeTextMessage(new JsonObject().put("type", "GAME_START").encode());
                }
              } else {
                if (hostSocket == null) {
                  hostSocket = ws;
                  setupSocket(ws, "HOST");
                  sendRole(ws, "HOST");
                  System.out.println("Host connected");
                } else if (guestSocket == null) {
                  guestSocket = ws;
                  setupSocket(ws, "GUEST");
                  sendRole(ws, "GUEST");
                  if (!gameStarted) {
                    System.out.println("Guest connected, ready to start the game");
                    startGame();
                  } else {
                    System.out.println("Guest reconnected");
                    if (guestReconnectTimerId != -1) {
                      vertx.cancelTimer(guestReconnectTimerId);
                      guestReconnectTimerId = -1;
                    }

                    ws.writeTextMessage(
                        new JsonObject()
                            .put("type", "GAME_START")
                            .put("isReconnect", true)
                            .encode());

                    vertx.setTimer(
                        500,
                        id -> {
                          JsonObject msg = new JsonObject().put("type", "GUEST_RECONNECTED");
                          if (hostSocket != null) hostSocket.writeTextMessage(msg.encode());
                          broadcastToSpectators(msg.encode());
                        });
                  }
                } else {
                  spectators.add(ws);
                  setupSocket(ws, "SPECTATOR");
                  sendRole(ws, "SPECTATOR");
                  System.out.println("Spectator connected, total spectators: " + spectators.size());
                  if (gameStarted) {
                    JsonObject msg = new JsonObject().put("type", "GAME_START");
                    ws.writeTextMessage(msg.encode());
                  }
                }
              }
            })
        .listen(
            8080,
            http -> {
              if (http.succeeded()) {
                System.out.println("Lobby server started on port 8080");
              } else {
                System.out.println("Failed to start lobby server: " + http.cause());
              }
            });
  }

  private void setupSocket(ServerWebSocket ws, String role) {
    if ("SPECTATOR".equals(role)) {
      ws.closeHandler(
          v -> {
            spectators.remove(ws);
            System.out.println("Spectator disconnected, total spectators: " + spectators.size());
          });
      return;
    }
    ws.textMessageHandler(
        text -> {
          JsonObject message = new JsonObject(text);
          String type = message.getString("type");

          if ("HOST_UPDATE".equals(type) && guestSocket != null) {
            guestSocket.writeTextMessage(text);
            broadcastToSpectators(text);
          } else if ("GUEST_UPDATE".equals(type) && hostSocket != null) {
            hostSocket.writeTextMessage(text);
            broadcastToSpectators(text);
          } else if ("GOAL_REACHED".equals(type) && gameStarted) {
            gameStarted = false;
            broadcastGameOver("GOAL_REACHED", role);
          } else if ("PLAYER_DIED".equals(type) && gameStarted) {
            gameStarted = false;
            String winner = role.equals("HOST") ? "GUEST" : "HOST";
            broadcastGameOver("PLAYER_DIED", winner);
          } else if ("ENTITY_DESTROYED".equals(type) && gameStarted) {
            if ("HOST".equals(role) && guestSocket != null) {
              guestSocket.writeTextMessage(text);
            } else if ("GUEST".equals(role) && hostSocket != null) {
              hostSocket.writeTextMessage(text);
            }
            broadcastToSpectators(text);
          } else if ("RESTORE_STATE".equals(type) && gameStarted) {
            if ("HOST".equals(role) && guestSocket != null) {
              guestSocket.writeTextMessage(text);
            }
          }
        });

    ws.closeHandler(
        v -> {
          if (gameStarted) {
            if ("GUEST".equals(role)) {
              System.out.println("Guest disconnected, starting 30 seconds timer for reconnection");
              guestSocket = null;

              JsonObject msg = new JsonObject().put("type", "GUEST_DISCONNECTED");
              if (hostSocket != null) hostSocket.writeTextMessage(msg.encode());
              broadcastToSpectators(msg.encode());

              guestReconnectTimerId =
                  vertx.setTimer(
                      30000,
                      id -> {
                        System.out.println(
                            "Guest did not reconnect in time, game over. Winner: HOST");
                        gameStarted = false;
                        broadcastGameOver("GUEST_TIMEOUT", "HOST");
                      });
            } else if ("HOST".equals(role)) {
              gameStarted = false;
              System.out.println("Host disconnected, game over. Winner: GUEST");
              broadcastGameOver("HOST_DISCONNECTED", "GUEST");
            }
          }
        });
  }

  private void sendRole(ServerWebSocket ws, String role) {
    JsonObject msg = new JsonObject().put("type", "ROLE_ASSIGNMENT").put("role", role);
    ws.writeTextMessage(msg.encode());
  }

  private void startGame() {
    gameStarted = true;
    JsonObject msg = new JsonObject().put("type", "GAME_START");
    String msgStr = msg.encode();
    hostSocket.writeTextMessage(msgStr);
    guestSocket.writeTextMessage(msgStr);
    broadcastToSpectators(msgStr);
    System.out.println("Game started");
  }

  private void broadcastToSpectators(String message) {
    for (ServerWebSocket spectator : spectators) {
      if (!spectator.isClosed()) {
        spectator.writeTextMessage(message);
      }
    }
  }

  private void broadcastGameOver(String reason, String winner) {
    JsonObject msg =
        new JsonObject().put("type", "GAME_OVER").put("reason", reason).put("winner", winner);
    String msgStr = msg.encode();
    if (hostSocket != null && !hostSocket.isClosed()) {
      hostSocket.writeTextMessage(msgStr);
    }
    if (guestSocket != null && !guestSocket.isClosed()) {
      guestSocket.writeTextMessage(msgStr);
    }
    broadcastToSpectators(msgStr);

    resetLobby();
  }

  private void resetLobby() {
    if (hostSocket != null && !hostSocket.isClosed()) hostSocket.close();
    if (guestSocket != null && !guestSocket.isClosed()) guestSocket.close();
    for (ServerWebSocket spectator : spectators) {
      if (!spectator.isClosed()) spectator.close();
    }
    spectators.clear();
    hostSocket = null;
    guestSocket = null;
    gameStarted = false;
    System.out.println("Lobby correctly reset, waiting for new connections");
  }
}
