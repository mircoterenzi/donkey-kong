package it.unibo.donkeykong.network.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // Inizializza il responder: slot libero e gioco non iniziato
    DiscoveryResponder responder =
        new DiscoveryResponder(vertx, testLobbyId, () -> true, () -> false, host -> {});
    responder.start();

    // Creiamo un socket fittizio per simulare un client che cerca una partita
    DatagramSocket clientSocket = vertx.createDatagramSocket(new DatagramSocketOptions());

    clientSocket.listen(
        0,
        "127.0.0.1",
        res -> {
          if (res.succeeded()) {
            clientSocket.handler(
                packet -> {
                  testContext.verify(
                      () -> {
                        JsonObject response = new JsonObject(packet.data().toString());
                        assertEquals("LOBBY", response.getString("type"));
                        assertEquals(testLobbyId, response.getString("lobbyId"));
                        assertTrue(response.getBoolean("guestSlotFree"));
                        testContext.completeNow();
                      });
                });

            // Invia un pacchetto DISCOVER al responder locale
            JsonObject discoverMsg = new JsonObject().put("type", "DISCOVER");
            clientSocket.send(
                Buffer.buffer(discoverMsg.encode()),
                Net.DISCOVERY_PORT,
                "127.0.0.1",
                sendRes -> {
                  assertTrue(sendRes.succeeded(), "Invio pacchetto UDP fallito");
                });
          } else {
            testContext.failNow(res.cause());
          }
        });
  }
}
