package it.unibo.donkeykong.network.protocol;

public final class Net {
  public static final int WS_PORT = 8080;
  public static final int DISCOVERY_PORT = 8081;
  public static final String OUTBOUND = "outbound.messages";

  public static String inbound(MessageType t) {
    return "inbound." + t.name().toLowerCase();
  }

  private Net() {}
}
