package it.unibo.donkeykong.network.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import it.unibo.donkeykong.network.protocol.Net;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
public class DiscoveryResponderTest {

  @Test
  void testResponderRepliesToDiscover(Vertx vertx, VertxTestContext testContext) {
    String testLobbyId = "test-lobby-id";
    DiscoveryResponder responder =
        new DiscoveryResponder(vertx, testLobbyId, () -> true, () -> false, host -> {});
    responder.start();

    DatagramSocket clientSocket = vertx.createDatagramSocket(new DatagramSocketOptions());
    clientSocket.listen(
        0,
        "127.0.0.1",
        res -> {
          if (res.succeeded()) {
            clientSocket.handler(
                packet ->
                    testContext.verify(
                        () -> {
                          JsonObject response = new JsonObject(packet.data().toString());
                          assertEquals("LOBBY", response.getString("type"));
                          testContext.completeNow();
                        }));

            vertx.setTimer(
                200,
                id -> {
                  JsonObject discoverMsg = new JsonObject().put("type", "DISCOVER");
                  clientSocket.send(
                      Buffer.buffer(discoverMsg.encode()),
                      Net.DISCOVERY_PORT,
                      "127.0.0.1",
                      sendRes -> {});
                });

          } else {
            testContext.failNow(res.cause());
          }
        });
  }
}
