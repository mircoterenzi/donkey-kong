package it.unibo.donkeykong.utilities;

import it.unibo.donkeykong.ecs.component.Position;

public class Constants {
  public static final int WORLD_WIDTH = 896;
  public static final int WORLD_HEIGHT = 1024;

  public static final double GRAVITY = 9.81;
  public static final double JUMP_FACTOR = 5.0;
  public static final double FALL_FACTOR = 3.0;
  public static final int BLOCKS_COLLISION = 2;

  public static final double PLAYER_VELOCITY = 7.0;
  public static final int PLAYER_COLLISION_RADIUS = 2;
  public static final int PLAYER_LIVES = 3;
  public static final Position FIRST_PLAYER_SPAWN = new Position(242, 260);
  public static final Position SECOND_PLAYER_SPAWN = new Position(300, 260);

  public static final double BARREL_VELOCITY = 4.0;
  public static final int BARREL_COLLISION_RADIUS = 2;

  public static final Position DK_POSITION = new Position(100, 900);
  public static final Position PAULINE_POSITION = new Position(800, 100);
}
