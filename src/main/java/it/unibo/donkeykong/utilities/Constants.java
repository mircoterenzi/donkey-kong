package it.unibo.donkeykong.utilities;

import it.unibo.donkeykong.ecs.component.Position;

public class Constants {
  public static final int WORLD_WIDTH = 896;
  public static final int WORLD_HEIGHT = 1024;
  public static final int BOTTOM_THRESHOLD = 980;

  public static final double GRAVITY = 9.81;
  public static final double JUMP_FACTOR = 22;
  public static final double FALL_FACTOR = 35;
  public static final float SPAWN_INTERVAL = 3000f;

  public static final double PLAYER_VELOCITY = 100;
  public static final double PLAYER_WIDTH = 16;
  public static final double PLAYER_HEIGHT = 16;
  public static final float PLAYER_FRAME_DURATION = 0.15f;
  public static final double PLAYER_SCALE = 3;
  public static final double PLAYER_BORDER = 1;
  public static final int PLAYER_COLLISION_WIDTH = 24;
  public static final int PLAYER_COLLISION_HEIGHT = 48;
  public static final int PLAYER_LIVES = 3;
  public static final Position FIRST_PLAYER_SPAWN = new Position(200, 969);
  public static final Position SECOND_PLAYER_SPAWN = new Position(700, 969);

  public static final Position RIGHT_BARREL_SPAWN = new Position(525, 324);
  public static final Position LEFT_BARREL_SPAWN = new Position(328, 334);
  public static final double BARREL_VELOCITY = 50;
  public static final double BARREL_WIDTH = 16;
  public static final double BARREL_HEIGHT = 16;
  public static final double BARREL_BORDER = 1;
  public static final double BARREL_SCALE = 3;
  public static final float BARREL_FRAME_DURATION = 0.15f;
  public static final int BARREL_COLLISION_RADIUS = 20;

  public static final Position DK_POSITION = new Position(432, 305);
  public static final double DK_WIDTH = 48;
  public static final double DK_HEIGHT = 32;
  public static final double DK_BORDER = 1;
  public static final double DK_SCALE = 3;
  public static final float DK_FRAME_DURATION = 150;
  public static final int DK_COLLISION_WIDTH = 144;
  public static final int DK_COLLISION_HEIGHT = 96;

  public static final Position PAULINE_POSITION = new Position(420, 177);
  public static final double PAULINE_WIDTH = 16;
  public static final double PAULINE_HEIGHT = 30;
  public static final double PAULINE_BORDER = 1;
  public static final double PAULINE_SCALE = 3;
  public static final float PAULINE_FRAME_DURATION = 1f;
  public static final int PAULINE_COLLISION_WIDTH = 48;
  public static final int PAULINE_COLLISION_HEIGHT = 90;
}
