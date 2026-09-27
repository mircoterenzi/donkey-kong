package it.unibo.donkeykong.network.protocol;

public record RoleMessage(MessageType type, Role role) {
  public RoleMessage(Role role) {
    this(MessageType.ROLE_ASSIGNMENT, role);
  }
}
