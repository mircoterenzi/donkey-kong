package it.unibo.donkeykong.network.messages;

public record RoleMessage(MessageType type, Role role) {
  public RoleMessage(Role role) {
    this(MessageType.ROLE_ASSIGNMENT, role);
  }
}
