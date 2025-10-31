package it.unibo.donkeykong.utilities;

import it.unibo.donkeykong.ecs.component.Position;

public class Constants {
  public static final int WORLD_WIDTH = 896;
  public static final int WORLD_HEIGHT = 1024;

  public static final double GRAVITY = 9.81;
  public static final double PLAYER_VELOCITY = 100;
  public static final double JUMP_FACTOR = 10;
  public static final double FALL_FACTOR = 3.0;
  public static final float SPAWN_INTERVAL = 3000f;

  public static final double PLAYER_VELOCITY = 7.0;
  public static final int PLAYER_WIDTH = 64;
  public static final int PLAYER_HEIGHT = 64;
  public static final int PLAYER_FRAME_DURATION = 100;
  public static final int PLAYER_COLLISION_RADIUS = 2;
  public static final int PLAYER_LIVES = 3;
  public static final Position FIRST_PLAYER_SPAWN = new Position(242, 260);
  public static final Position SECOND_PLAYER_SPAWN = new Position(300, 260);

  public static final Position RIGHT_BARREL_SPAWN = new Position(525, 334);
  public static final Position LEFT_BARREL_SPAWN = new Position(328, 334);
  public static final double BARREL_VELOCITY = 4.0;
  public static final int BARREL_WIDTH = 48;
  public static final int BARREL_HEIGHT = 48;
  public static final int BARREL_FRAME_DURATION = 150;
  public static final int BARREL_COLLISION_RADIUS = 2;

  public static final Position DK_POSITION = new Position(100, 900);
  public static final int DK_WIDTH = 96;
  public static final int DK_HEIGHT = 96;
  public static final int DK_FRAME_DURATION = 150;
  public static final int DK_COLLISION_RADIUS = 3;
  public static final Position PAULINE_POSITION = new Position(800, 100);
  public static final int PAULINE_WIDTH = 64;
  public static final int PAULINE_HEIGHT = 64;
  public static final int PAULINE_FRAME_DURATION = 100;
  public static final int PAULINE_COLLISION_RADIUS = 2;
}
