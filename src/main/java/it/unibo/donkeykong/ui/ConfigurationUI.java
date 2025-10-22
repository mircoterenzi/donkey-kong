package it.unibo.donkeykong.ui;

import java.util.Map;

public final class ConfigurationUI {

  private ConfigurationUI() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static final int WINDOW_WIDTH = 896;
  public static final int WINDOW_HEIGHT = 1024;
  public static final String WINDOW_TITLE = "Donkey Kong: Rush";
  public static final String ICON_PATH = "/images/donkey_kong_icon.png";

  public static final Map<String, String> ASSET_PATHS =
      Map.of(
          "player", "/sprites/player.png",
          "barrel", "/sprites/barrel.png",
          "background", "/images/level1.png");
}
