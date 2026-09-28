package it.unibo.donkeykong.network.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import it.unibo.donkeykong.network.protocol.MessageType;
import it.unibo.donkeykong.network.protocol.Net;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class ClientVerticleTest {
  private HttpServer mockServer;

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext) {
    mockServer =
        vertx
            .createHttpServer()
            .webSocketHandler(
                ws -> {
                  if ("/play".equals(ws.path())) {
                    JsonObject roleMsg =
                        new JsonObject().put("type", "ROLE_ASSIGNMENT").put("role", "GUEST");
                    ws.writeTextMessage(roleMsg.encode());
                    ws.textMessageHandler(
                        text -> {
                          JsonObject msg = new JsonObject(text);
                          if ("TEST_MSG".equals(msg.getString("type"))) {
                            vertx.eventBus().publish("test.server.received", text);
                          }
                        });
                  }
                })
            .listen(8080, "localhost", testContext.succeeding(server -> testContext.completeNow()));
  }

  @AfterEach
  void tearDown(VertxTestContext testContext) {
    mockServer.close(testContext.succeeding(v -> testContext.completeNow()));
  }

  @Test
  void testClientReceivesAndPublishesRoleAssignment(Vertx vertx, VertxTestContext testContext) {
    vertx
        .eventBus()
        .<JsonObject>consumer(
            Net.inbound(MessageType.ROLE_ASSIGNMENT),
            msg ->
                testContext.verify(
                    () -> {
                      assertEquals(
                          "GUEST",
                          msg.body().getString("role"),
                          "Il ruolo assegnato dovrebbe essere GUEST");
                      testContext.completeNow();
                    }));
    vertx.deployVerticle(new ClientVerticle("/play", "localhost"));
  }

  @Test
  void testClientForwardsOutboundMessagesToWebSocket(Vertx vertx, VertxTestContext testContext) {
    vertx
        .eventBus()
        .<String>consumer(
            "test.server.received",
            msg ->
                testContext.verify(
                    () -> {
                      JsonObject json = new JsonObject(msg.body());
                      assertEquals("TEST_MSG", json.getString("type"));
                      testContext.completeNow();
                    }));
    vertx
        .eventBus()
        .<JsonObject>consumer(
            Net.inbound(MessageType.ROLE_ASSIGNMENT),
            msg -> {
              JsonObject testMsg = new JsonObject().put("type", "TEST_MSG");
              vertx.eventBus().publish(Net.OUTBOUND, testMsg);
            });
    vertx.deployVerticle(new ClientVerticle("/play", "localhost"));
  }
}
