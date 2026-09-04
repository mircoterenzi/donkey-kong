package it.unibo.donkeykong.network.messages;

public record GuestUpdateMessage(
  MessageType type,
  String playerId,
  double playerX,
  double playerY,
  String playerState,
  String playerDirection
) {
  public GuestUpdateMessage(String playerId, double playerX, double playerY,
                            String playerState, String playerDirection) {
    this(MessageType.GUEST_UPDATE, playerId, playerX, playerY, playerState, playerDirection);
  }
}
