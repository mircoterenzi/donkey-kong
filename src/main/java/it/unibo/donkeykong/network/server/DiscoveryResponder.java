package it.unibo.donkeykong.network.server;

import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.json.JsonObject;
import it.unibo.donkeykong.network.discovery.DiscoveryClient;
import it.unibo.donkeykong.network.protocol.Net;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Handles UDP discovery and conflict resolution (split-brain). */
public class DiscoveryResponder {
  private final Vertx vertx;
  private final String lobbyId;
  private final Supplier<Boolean> isGuestSlotFree;
  private final Supplier<Boolean> isGameStarted;
  private final Consumer<String> onYield;

  public DiscoveryResponder(
      Vertx vertx,
      String lobbyId,
      Supplier<Boolean> isGuestSlotFree,
      Supplier<Boolean> isGameStarted,
      Consumer<String> onYield) {
    this.vertx = vertx;
    this.lobbyId = lobbyId;
    this.isGuestSlotFree = isGuestSlotFree;
    this.isGameStarted = isGameStarted;
    this.onYield = onYield;
  }

  public void start() {
    DatagramSocket udpSocket =
        vertx.createDatagramSocket(new DatagramSocketOptions().setBroadcast(true));

    udpSocket.listen(
        Net.DISCOVERY_PORT,
        "0.0.0.0",
        res -> {
          if (res.succeeded()) {
            udpSocket.handler(
                packet -> {
                  try {
                    JsonObject msg = new JsonObject(packet.data().toString());
                    String type = msg.getString("type");

                    if ("DISCOVER".equals(type)) {
                      JsonObject reply =
                          new JsonObject()
                              .put("type", "LOBBY")
                              .put("wsPort", Net.WS_PORT)
                              .put("lobbyId", lobbyId)
                              .put("guestSlotFree", isGuestSlotFree.get())
                              .put("gameStarted", isGameStarted.get());
                      udpSocket.send(
                          reply.encode(), packet.sender().port(), packet.sender().host(), r -> {});

                    } else if ("LOBBY".equals(type)) {
                      if (isGuestSlotFree.get() && !isGameStarted.get()) {
                        String otherId = msg.getString("lobbyId");
                        if (otherId != null
                            && Boolean.TRUE.equals(msg.getBoolean("guestSlotFree", false))
                            && otherId.compareTo(lobbyId) < 0) {
                          System.out.println(
                              "Split-brain: higher priority lobby found. Yielding host role.");
                          onYield.accept(packet.sender().host());
                        }
                      }
                    }
                  } catch (io.vertx.core.json.DecodeException | ClassCastException e) {
                    System.err.println("Discarded malformed UDP packet.");
                  }
                });

            vertx.setPeriodic(
                2000,
                id -> {
                  if (isGuestSlotFree.get() && !isGameStarted.get()) {
                    DiscoveryClient.broadcast(udpSocket);
                  }
                });
          } else {
            System.out.println("Failed to start UDP discovery server: " + res.cause());
          }
        });
  }
}
