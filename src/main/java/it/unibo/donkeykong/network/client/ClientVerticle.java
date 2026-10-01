package it.unibo.donkeykong.network.client;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import it.unibo.donkeykong.network.protocol.MessageType;
import it.unibo.donkeykong.network.protocol.Net;

/**
 * ClientVerticle is responsible for managing the WebSocket connection to the server. It handles
 * incoming messages and publishes them to the event bus for other components to consume.
 */
public class ClientVerticle extends AbstractVerticle {
  private final String uri;
  private final String hostIp;

  public ClientVerticle(String uri, String hostIp) {
    this.uri = uri;
    this.hostIp = hostIp;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    startPromise.complete();

    vertx
        .createWebSocketClient()
        .connect(new WebSocketConnectOptions().setHost(hostIp).setPort(Net.WS_PORT).setURI(uri))
        .onSuccess(
            ws -> {
              ws.textMessageHandler(this::forward);

              ws.closeHandler(
                  v -> {
                    System.out.println("Disconnected from server");
                    vertx
                        .eventBus()
                        .publish(
                            "game.disconnected",
                            new JsonObject().put("deploymentId", deploymentID()));
                  });

              vertx
                  .eventBus()
                  .<JsonObject>consumer(Net.OUTBOUND, m -> ws.writeTextMessage(m.body().encode()));
            })
        .onFailure(
            err -> {
              System.out.println("Failed to connect to server: " + err.getMessage());
              vertx
                  .eventBus()
                  .publish(
                      "game.disconnected", new JsonObject().put("deploymentId", deploymentID()));
            });
  }

  private void forward(String text) {
    try {
      JsonObject msg = new JsonObject(text);
      String typeString = msg.getString("type");

      if (typeString == null) {
        System.err.println("Discarded WS message: missing 'type' field.");
        return;
      }

      MessageType type = MessageType.valueOf(typeString);
      vertx.eventBus().publish(Net.inbound(type), msg);
    } catch (DecodeException e) {
      System.err.println("Discarded malformed WS message (not JSON).");
    } catch (IllegalArgumentException e) {
      System.err.println("Discarded WS message with unknown type: " + text);
    }
  }
}
