package it.unibo.donkeykong.network.client;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;

/**
 * ClientVerticle is responsible for managing the WebSocket connection to the server. It handles
 * incoming messages and publishes them to the event bus for other components to consume.
 */
public class ClientVerticle extends AbstractVerticle {

  private final String uri;
  private WebSocket webSocket;
  private String myRole;

  public ClientVerticle(String uri) {
    this.uri = uri;
  }

  /**
   * Starts the verticle and establishes a WebSocket connection to the server. It sets up handlers
   * for incoming messages and publishes them to the event bus.
   */
  @Override
  public void start() {
    WebSocketClient client = vertx.createWebSocketClient();

    WebSocketConnectOptions options =
        new WebSocketConnectOptions().setHost("localhost").setPort(8080).setURI(uri);

    client.connect(
        options,
        res -> {
          if (res.succeeded()) {
            webSocket = res.result();
            webSocket.textMessageHandler(this::handleIncomingMessage);

            vertx
                .eventBus()
                .<JsonObject>consumer(
                    "outbound.messages",
                    msg -> {
                      if (webSocket != null && !webSocket.isClosed()) {
                        webSocket.writeTextMessage(msg.body().encode());
                      }
                    });

            webSocket.closeHandler(
                v -> {
                  System.out.println("Disconnected from server");
                  vertx.eventBus().publish("game.disconnected", new JsonObject());
                });
          } else {
            System.out.println("Failed to connect to server: " + res.cause().getMessage());
          }
        });
  }

  private void handleIncomingMessage(String text) {
    JsonObject message = new JsonObject(text);
    String type = message.getString("type");

    switch (type) {
      case "ROLE_ASSIGNMENT" -> {
        myRole = message.getString("role");
        vertx.eventBus().publish("game.role", myRole);
        System.out.println("Assigned role: " + myRole);
      }
      case "GAME_START" -> {
        vertx.eventBus().publish("game.start", message);
        System.out.println("Game started");
      }
      case "HOST_UPDATE" -> {
        if (!"HOST".equals(myRole)) {
          vertx.eventBus().publish("inbound.host_update", message);
          System.out.println("Received host update: " + message.encode());
        }
      }
      case "GUEST_UPDATE" -> {
        if (!"GUEST".equals(myRole)) {
          vertx.eventBus().publish("inbound.guest_update", message);
          System.out.println("Received guest update: " + message.encode());
        }
      }
      case "GAME_OVER" -> {
        String winner = message.getString("winner");
        String reason = message.getString("reason");
        System.out.println("Game Over! Winner: " + winner + " | Reason: " + reason);
        vertx.eventBus().publish("game.over", message);
      }
      case "ENTITY_DESTROYED" -> {
        vertx.eventBus().publish("inbound.entity_destroyed", message);
        System.out.println("Entity destroyed: " + message.encode());
      }
      case "GUEST_DISCONNECTED" -> {
        vertx.eventBus().publish("inbound.guest_disconnected", message);
        System.out.println("Guest disconnected: " + message.encode());
      }
      case "GUEST_RECONNECTED" -> {
        vertx.eventBus().publish("inbound.guest_reconnected", message);
        System.out.println("Guest reconnected: " + message.encode());
      }
      case "RESTORE_STATE" -> {
        System.out.println("Received restore state message: " + message.encode());
        vertx.eventBus().publish("inbound.restore_state", message);
      }
      default -> System.out.println("Impossible to handle message of type: " + type);
    }
  }
}
