package it.unibo.donkeykong.network.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import it.unibo.donkeykong.network.discovery.DiscoveryResponder;
import it.unibo.donkeykong.network.protocol.MessageType;
import it.unibo.donkeykong.network.protocol.Net;
import it.unibo.donkeykong.network.protocol.Role;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * LobbyVerticle is a Vert.x verticle that manages the game lobby for a multiplayer game. It handles
 * WebSocket connections, delegates UDP discovery, and manages game state.
 */
public class LobbyVerticle extends AbstractVerticle {
  private ServerWebSocket hostSocket;
  private ServerWebSocket guestSocket;
  private final List<ServerWebSocket> spectators = new ArrayList<>();

  private boolean gameStarted = false;
  private long guestReconnectTimerId = -1;
  private final String lobbyId = UUID.randomUUID().toString();

  @Override
  public void start() {
    DiscoveryResponder discoveryResponder =
        new DiscoveryResponder(
            vertx,
            lobbyId,
            () -> guestSocket == null,
            () -> gameStarted,
            host -> {
              vertx.eventBus().publish("lobby.yield", host);
              resetLobby();
              vertx.undeploy(context.deploymentID());
            });
    discoveryResponder.start();

    vertx
        .createHttpServer()
        .webSocketHandler(
            ws -> {
              if ("/spectate".equals(ws.path())) {
                onSpectator(ws);
              } else {
                if (hostSocket == null) {
                  hostSocket = ws;
                  onPlayer(ws, Role.HOST);
                  System.out.println("Host connected");
                } else if (guestSocket == null) {
                  guestSocket = ws;
                  if (!gameStarted) {
                    onPlayer(ws, Role.GUEST);
                    System.out.println("Guest connected, ready to start the game");
                    startGame();
                  } else {
                    onGuestReconnect(ws);
                  }
                } else { // If there are no slots available, treat as spectator
                  onSpectator(ws);
                }
              }
            })
        .listen(
            Net.WS_PORT,
            http -> {
              if (http.succeeded()) {
                System.out.println("Lobby server started on port " + Net.WS_PORT);
              } else {
                System.out.println("Failed to start lobby server: " + http.cause());
              }
            });
  }

  private void onSpectator(ServerWebSocket ws) {
    spectators.add(ws);
    ws.closeHandler(
        v -> {
          spectators.remove(ws);
          System.out.println("Spectator disconnected, total spectators: " + spectators.size());
        });
    sendRole(ws, Role.SPECTATOR);
    System.out.println("Spectator connected, total spectators: " + spectators.size());

    if (gameStarted) {
      ws.writeTextMessage(new JsonObject().put("type", "GAME_START").encode());
    }
  }

  private void onPlayer(ServerWebSocket ws, Role role) {
    sendRole(ws, role);
    setupMessageHandlers(ws, role);

    ws.closeHandler(
        v -> {
          if (gameStarted) {
            if (role == Role.GUEST) {
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
                        endGame("GUEST_TIMEOUT", Role.HOST);
                      });
            } else if (role == Role.HOST) {
              System.out.println("Host disconnected, game over. Winner: GUEST");
              endGame("HOST_DISCONNECTED", Role.GUEST);
            }
          } else {
            if (role == Role.GUEST) {
              guestSocket = null;
            } else if (role == Role.HOST) {
              resetLobby();
            }
          }
        });
  }

  private void onGuestReconnect(ServerWebSocket ws) {
    onPlayer(ws, Role.GUEST);
    System.out.println("Guest reconnected");

    if (guestReconnectTimerId != -1) {
      vertx.cancelTimer(guestReconnectTimerId);
      guestReconnectTimerId = -1;
    }

    ws.writeTextMessage(
        new JsonObject().put("type", "GAME_START").put("isReconnect", true).encode());

    vertx.setTimer(
        500,
        id -> {
          JsonObject msg = new JsonObject().put("type", "GUEST_RECONNECTED");
          if (hostSocket != null) hostSocket.writeTextMessage(msg.encode());
          broadcastToSpectators(msg.encode());
        });
  }

  private void setupMessageHandlers(ServerWebSocket ws, Role role) {
    ws.textMessageHandler(
        text -> {
          try {
            JsonObject message = new JsonObject(text);
            String typeString = message.getString("type");

            if (typeString == null) {
              System.err.println("Discarded WS message: missing 'type' field.");
              return;
            }

            MessageType type = MessageType.valueOf(typeString);

            switch (type) {
              case HOST_UPDATE -> {
                message.mapTo(it.unibo.donkeykong.network.protocol.HostUpdateMessage.class);
                send(role.opponent(), text);
                broadcastToSpectators(text);
              }
              case GUEST_UPDATE -> {
                message.mapTo(it.unibo.donkeykong.network.protocol.GuestUpdateMessage.class);
                send(role.opponent(), text);
                broadcastToSpectators(text);
              }
              case ENTITY_DESTROYED -> {
                if (message.getString("id") == null)
                  throw new IllegalArgumentException("Missing id");
                if (gameStarted) {
                  send(role.opponent(), text);
                  broadcastToSpectators(text);
                }
              }
              case RESTORE_STATE -> {
                if (message.getDouble("playerX") == null)
                  throw new IllegalArgumentException("Malformed state");
                if (gameStarted && role == Role.HOST) send(Role.GUEST, text);
              }
              case GOAL_REACHED -> {
                if (gameStarted) endGame("GOAL_REACHED", role);
              }
              case PLAYER_DIED -> {
                if (gameStarted) endGame("PLAYER_DIED", role.opponent());
              }
              default -> System.out.println("Ignored: " + type);
            }
          } catch (io.vertx.core.json.DecodeException e) {
            System.err.println("Discarded malformed WS message (not JSON).");
          } catch (IllegalArgumentException e) {
            System.err.println("Discarded WS message with unknown type or invalid schema: " + text);
          }
        });
  }

  private void send(Role targetRole, String message) {
    if (targetRole == Role.HOST && hostSocket != null && !hostSocket.isClosed()) {
      hostSocket.writeTextMessage(message);
    } else if (targetRole == Role.GUEST && guestSocket != null && !guestSocket.isClosed()) {
      guestSocket.writeTextMessage(message);
    }
  }

  private void sendRole(ServerWebSocket ws, Role role) {
    JsonObject msg = new JsonObject().put("type", "ROLE_ASSIGNMENT").put("role", role.name());
    ws.writeTextMessage(msg.encode());
  }

  private void startGame() {
    gameStarted = true;
    JsonObject msg = new JsonObject().put("type", "GAME_START");
    String msgStr = msg.encode();
    if (hostSocket != null) hostSocket.writeTextMessage(msgStr);
    if (guestSocket != null) guestSocket.writeTextMessage(msgStr);
    broadcastToSpectators(msgStr);
  }

  private void broadcastToSpectators(String message) {
    for (ServerWebSocket spectator : spectators) {
      if (!spectator.isClosed()) spectator.writeTextMessage(message);
    }
  }

  private void endGame(String reason, Role winner) {
    gameStarted = false;
    broadcastGameOver(reason, winner.name());
  }

  private void broadcastGameOver(String reason, String winner) {
    JsonObject msg =
        new JsonObject().put("type", "GAME_OVER").put("reason", reason).put("winner", winner);
    String msgStr = msg.encode();

    if (hostSocket != null && !hostSocket.isClosed()) hostSocket.writeTextMessage(msgStr);
    if (guestSocket != null && !guestSocket.isClosed()) guestSocket.writeTextMessage(msgStr);
    broadcastToSpectators(msgStr);

    resetLobby();
  }

  private void resetLobby() {
    if (guestReconnectTimerId != -1) {
      vertx.cancelTimer(guestReconnectTimerId);
      guestReconnectTimerId = -1;
    }
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
