package it.unibo.donkeykong.network.messages;

public record GuestUpdateMessage(
    MessageType type, double playerX, double playerY, String playerState, String playerDirection) {
  public GuestUpdateMessage(
      double playerX, double playerY, String playerState, String playerDirection) {
    this(MessageType.GUEST_UPDATE, playerX, playerY, playerState, playerDirection);
  }
}
