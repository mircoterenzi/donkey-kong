package it.unibo.donkeykong.network.protocol;

public record GameStartMessage(MessageType type) {
  public GameStartMessage() {
    this(MessageType.GAME_START);
  }
}
