package it.unibo.donkeykong.network.protocol;

public record GoalReachedMessage(MessageType type) {
  public GoalReachedMessage() {
    this(MessageType.GOAL_REACHED);
  }
}
