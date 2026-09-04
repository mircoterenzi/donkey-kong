package it.unibo.donkeykong.client.network;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;

/**
 * ClientVerticle is responsible for managing the WebSocket connection to the server. It handles
 * incoming messages and publishes them to the event bus for other components to consume.
 */
public class ClientVerticle extends AbstractVerticle {

  private WebSocket webSocket;
  private String myRole;

  /**
   * Starts the verticle and establishes a WebSocket connection to the server. It sets up handlers
   * for incoming messages and publishes them to the event bus.
   */
  @Override
  public void start() {
    WebSocketClient client = vertx.createWebSocketClient();

    WebSocketConnectOptions options =
        new WebSocketConnectOptions().setHost("localhost").setPort(8080).setURI("/");

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
        vertx.eventBus().publish("game.start", new JsonObject());
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
      default -> System.out.println("Impossible to handle message of type: " + type);
    }
  }
}
