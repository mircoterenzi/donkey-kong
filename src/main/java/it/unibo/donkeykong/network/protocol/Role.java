package it.unibo.donkeykong.network.protocol;

public enum Role {
  HOST,
  GUEST,
  SPECTATOR;

  public Role opponent() {
    return this == HOST ? GUEST : HOST;
  }
}
