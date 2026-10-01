package it.unibo.donkeykong.network.discovery;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.json.JsonObject;
import it.unibo.donkeykong.network.protocol.Net;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class DiscoveryClient {
  private final Vertx vertx;
  private final Random random = new Random();

  public DiscoveryClient(Vertx vertx) {
    this.vertx = vertx;
  }

  public Future<String> discoverPlay() {
    Promise<String> promise = Promise.promise();
    DatagramSocket socket =
        vertx.createDatagramSocket(new DatagramSocketOptions().setBroadcast(true));

    socket.handler(
        packet -> {
          JsonObject msg = new JsonObject(packet.data().toString());
          if ("LOBBY".equals(msg.getString("type")) && msg.getBoolean("guestSlotFree", false)) {
            promise.tryComplete(packet.sender().host());
          }
        });

    broadcast(socket);
    long jitter = random.nextInt(501);

    vertx.setTimer(
        1500 + jitter,
        id -> {
          socket.close();
          promise.tryFail("No lobby available. Host deployment required.");
        });

    return promise.future();
  }

  public Future<String> discoverSpectate() {
    Promise<String> promise = Promise.promise();
    DatagramSocket socket =
        vertx.createDatagramSocket(new DatagramSocketOptions().setBroadcast(true));
    System.out.println("Searching for games...");

    socket.handler(
        packet -> {
          JsonObject msg = new JsonObject(packet.data().toString());
          if ("LOBBY".equals(msg.getString("type"))) {
            if (promise.tryComplete(packet.sender().host())) {
              socket.close();
            }
          }
        });

    broadcast(socket);

    AtomicInteger attempts = new AtomicInteger(0);
    final int MAX_ATTEMPTS = 5;

    vertx.setPeriodic(
        2000,
        id -> {
          if (promise.future().isComplete()) {
            vertx.cancelTimer(id);
            return;
          }

          if (attempts.getAndIncrement() >= MAX_ATTEMPTS) {
            vertx.cancelTimer(id);
            socket.close();
            promise.tryFail("Timeout: no game found.");
          } else {
            System.out.println("Searching for games...");
            broadcast(socket);
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
            socket.send(buffer, Net.DISCOVERY_PORT, broadcastAddress.getHostAddress(), res -> {});
          }
        }
      }
    } catch (SocketException e) {
      System.err.println("Error during broadcast: " + e.getMessage());
    }
  }
}
