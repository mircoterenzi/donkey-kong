package it.unibo.donkeykong.network.messages;

public record GuestUpdateMessage(
    MessageType type,
    double playerX,
    double playerY,
    String playerState,
    String playerDirection,
    int lives) {
  public GuestUpdateMessage(
      double playerX, double playerY, String playerState, String playerDirection, int lives) {
    this(MessageType.GUEST_UPDATE, playerX, playerY, playerState, playerDirection, lives);
  }
}
