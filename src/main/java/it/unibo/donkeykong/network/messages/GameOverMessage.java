package it.unibo.donkeykong.network.messages;

public record GameOverMessage(
  MessageType type,
  String reason,
  Role winner
) {
  public GameOverMessage(String reason, Role winner) {
    this(MessageType.GAME_OVER, reason, winner);
  }
}
