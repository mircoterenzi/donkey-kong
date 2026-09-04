package it.unibo.donkeykong.network.messages;

import java.util.List;

public record HostUpdateMessage(
  MessageType type,
  double playerX,
  double playerY,
  String playerState,
  String playerDirection,
  List<BarrelData> barrels
) {
  public HostUpdateMessage(double playerX, double playerY, String playerState,
                           String playerDirection, List<BarrelData> barrels) {
    this(MessageType.HOST_UPDATE, playerX, playerY, playerState, playerDirection, barrels);
  }
}
