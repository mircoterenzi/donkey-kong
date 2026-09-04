package it.unibo.donkeykong.network.messages;

public record GoalReachedMessage(
  MessageType type
) {
  public GoalReachedMessage() {
    this(MessageType.GOAL_REACHED);
  }
}
