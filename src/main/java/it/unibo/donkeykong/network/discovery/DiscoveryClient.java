package it.unibo.donkeykong.network.discovery;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.json.JsonObject;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;

public class DiscoveryClient {
  private final Vertx vertx;
  private final Random random = new Random();
  private static final int DISCOVERY_PORT = 8081;

  public DiscoveryClient(Vertx vertx) {
    this.vertx = vertx;
  }

  public Future<String> discoverPlay() {
    Promise<String> promise = Promise.promise();
    DatagramSocket socket =
        vertx.createDatagramSocket(new DatagramSocketOptions().setBroadcast(true));
    String[] foundIp = new String[1]; // Memorizza la prima lobby valida trovata

    socket.handler(
        packet -> {
          JsonObject msg = new JsonObject(packet.data().toString());
          if ("LOBBY".equals(msg.getString("type")) && msg.getBoolean("guestSlotFree", false)) {
            if (foundIp[0] == null) {
              foundIp[0] = packet.sender().host();
            }
          }
        });

    broadcast(socket);

    long jitter = random.nextInt(501);
    vertx.setTimer(
        1500 + jitter,
        id -> {
          socket.close();
          if (foundIp[0] != null) {
            promise.complete(foundIp[0]);
          } else {
            promise.fail("Nessuna lobby disponibile. Necessario deploy Host.");
          }
        });

    return promise.future();
  }

  public Future<String> discoverSpectate() {
    Promise<String> promise = Promise.promise();
    DatagramSocket socket =
        vertx.createDatagramSocket(new DatagramSocketOptions().setBroadcast(true));
    System.out.println("Searching for games…");

    socket.handler(
        packet -> {
          JsonObject msg = new JsonObject(packet.data().toString());
          if ("LOBBY".equals(msg.getString("type"))) {
            if (!promise.future().isComplete()) {
              promise.complete(packet.sender().host());
              socket.close();
            }
          }
        });

    broadcast(socket);

    vertx.setPeriodic(
        2000,
        new io.vertx.core.Handler<>() {
          int attempts = 0;
          final int MAX_ATTEMPTS = 5;

          @Override
          public void handle(Long id) {
            if (promise.future().isComplete()) {
              vertx.cancelTimer(id);
              return;
            }

            if (attempts >= MAX_ATTEMPTS) {
              vertx.cancelTimer(id);
              socket.close();
              promise.fail("Timeout: nessuna partita trovata.");
            } else {
              System.out.println("Searching for games…");
              broadcast(socket);
              attempts++;
            }
          }
        });

    return promise.future();
  }

  public static void broadcast(DatagramSocket socket) {
    JsonObject msg = new JsonObject().put("type", "DISCOVER").put("proto", 1);
    Buffer buffer = Buffer.buffer(msg.encode());
    try {
      Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        NetworkInterface ni = interfaces.nextElement();
        if (ni.isLoopback() || !ni.isUp()) continue;
        for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
          InetAddress broadcastAddress = ia.getBroadcast();
          if (broadcastAddress != null) {
            socket.send(buffer, DISCOVERY_PORT, broadcastAddress.getHostAddress(), res -> {});
          }
        }
      }
    } catch (SocketException e) {
      System.err.println("Errore durante il broadcast: " + e.getMessage());
    }
  }
}
