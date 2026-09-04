package it.unibo.donkeykong.network.messages;

public record GameStartMessage(MessageType type) {
  public GameStartMessage() {
    this(MessageType.GAME_START);
  }
}
