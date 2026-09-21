package it.unibo.donkeykong.network.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocketClient;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class LobbyVerticleTest {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(
        new LobbyVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  void testHostAndGuestConnectionTriggersGameStart(Vertx vertx, VertxTestContext testContext) {
    WebSocketClient client = vertx.createWebSocketClient();
    WebSocketConnectOptions options =
        new WebSocketConnectOptions().setHost("localhost").setPort(8080).setURI("/");

    client.connect(
        options,
        testContext.succeeding(
            hostWs ->
                hostWs.textMessageHandler(
                    msg -> {
                      JsonObject json = new JsonObject(msg);
                      if ("ROLE_ASSIGNMENT".equals(json.getString("type"))) {
                        assertEquals("HOST", json.getString("role"));

                        client.connect(
                            options,
                            testContext.succeeding(
                                guestWs ->
                                    guestWs.textMessageHandler(
                                        guestMsg -> {
                                          JsonObject guestJson = new JsonObject(guestMsg);
                                          if ("GAME_START".equals(guestJson.getString("type"))) {
                                            testContext.completeNow();
                                          }
                                        })));
                      }
                    })));
  }

  @Test
  void testSpectatorConnectionAndBroadcast(Vertx vertx, VertxTestContext testContext) {
    WebSocketClient client = vertx.createWebSocketClient();
    WebSocketConnectOptions defaultOptions =
        new WebSocketConnectOptions().setHost("localhost").setPort(8080).setURI("/");
    WebSocketConnectOptions spectateOptions =
        new WebSocketConnectOptions().setHost("localhost").setPort(8080).setURI("/spectate");

    client.connect(
        defaultOptions,
        testContext.succeeding(
            hostWs ->
                client.connect(
                    defaultOptions,
                    testContext.succeeding(
                        guestWs ->
                            client.connect(
                                spectateOptions,
                                testContext.succeeding(
                                    spectatorWs ->
                                        spectatorWs.textMessageHandler(
                                            msg -> {
                                              JsonObject json = new JsonObject(msg);
                                              String type = json.getString("type");

                                              if ("ROLE_ASSIGNMENT".equals(type)) {
                                                assertEquals("SPECTATOR", json.getString("role"));
                                              } else if ("GAME_START".equals(type)) {
                                                JsonObject updateMsg =
                                                    new JsonObject()
                                                        .put("type", "HOST_UPDATE")
                                                        .put("playerX", 100.0)
                                                        .put("playerY", 200.0);
                                                hostWs.writeTextMessage(updateMsg.encode());
                                              } else if ("HOST_UPDATE".equals(type)) {
                                                assertEquals(100.0, json.getDouble("playerX"));
                                                assertEquals(200.0, json.getDouble("playerY"));
                                                testContext.completeNow();
                                              }
                                            })))))));
  }
}
