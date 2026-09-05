package it.unibo.donkeykong.ecs.entity;

import static it.unibo.donkeykong.core.Constants.*;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.GraphicComponent.*;
import it.unibo.donkeykong.ecs.component.StateComponent.*;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.entity.api.EntityFactory;

/** Implementation of the EntityFactory interface responsible for creating various game entities. */
public record EntityFactoryImpl(World world, String myRole) implements EntityFactory {

  private Entity buildPlayer(
      String roleId, String roleType, PositionComponent spawnPos, String spritePath) {
    Entity player =
        world
            .createEntity()
            .addComponent(new NetworkComponent(roleId, roleType))
            .addComponent(spawnPos)
            .addComponent(new StateComponent(State.IDLE, Direction.RIGHT))
            .addComponent(
                new GraphicComponent(
                    spritePath,
                    PLAYER_WIDTH,
                    PLAYER_HEIGHT,
                    PLAYER_BORDER,
                    PLAYER_SCALE,
                    PLAYER_FRAME_DURATION,
                    (state) ->
                        switch (state) {
                          case MOVING -> new AnimationSettings(1, 0, 2);
                          case JUMP, FALL -> new AnimationSettings(3, 0, 1);
                          case FAST_FALL -> new AnimationSettings(4, 0, 1);
                          case UP, DOWN -> new AnimationSettings(5, 0, 2);
                          case STOP_CLIMB -> new AnimationSettings(5, 0, 1);
                          default -> new AnimationSettings(0, 0, 1);
                        }));
    if (roleType.equals(this.myRole)) {
      player
          .addComponent(new InputComponent())
          .addComponent(new GravityComponent(GRAVITY))
          .addComponent(new VelocityComponent(0, 0))
          .addComponent(new HealthComponent(PLAYER_LIVES))
          .addComponent(new RectangleCollider(PLAYER_COLLISION_WIDTH, PLAYER_COLLISION_HEIGHT));
    }
    return player;
  }

  @Override
  public Entity createFirstPlayer() {
    return buildPlayer("player-host", "HOST", FIRST_PLAYER_SPAWN, "/sprites/mario.png");
  }

  @Override
  public Entity createSecondPlayer() {
    return buildPlayer("player-guest", "GUEST", SECOND_PLAYER_SPAWN, "/sprites/luigi.png");
  }

  @Override
  public Entity createPauline() {
    return world
        .createEntity()
        .addComponent(PAULINE_POSITION)
        .addComponent(new RectangleCollider(PAULINE_COLLISION_WIDTH, PAULINE_COLLISION_HEIGHT))
        .addComponent(new StateComponent(State.IDLE, Direction.RIGHT))
        .addComponent(new GoalComponent())
        .addComponent(
            new GraphicComponent(
                "/sprites/pauline.png",
                PAULINE_WIDTH,
                PAULINE_HEIGHT,
                PAULINE_BORDER,
                PAULINE_SCALE,
                PAULINE_FRAME_DURATION,
                (state) -> new AnimationSettings(2, 0, 2)));
  }

  @Override
  public Entity createDonkeyKong() {
    return world
        .createEntity()
        .addComponent(DK_POSITION)
        .addComponent(new RectangleCollider(DK_COLLISION_WIDTH, DK_COLLISION_HEIGHT))
        .addComponent(new StateComponent(State.IDLE, Direction.RIGHT))
        .addComponent(
            new GraphicComponent(
                "/sprites/donkey.png",
                DK_WIDTH,
                DK_HEIGHT,
                DK_BORDER,
                DK_SCALE,
                DK_FRAME_DURATION,
                (state) -> new AnimationSettings(0, 1, 4)));
  }

  private Entity buildBarrelEntity(String id, PositionComponent pos, double velocity) {
    Direction direction = velocity < 0 ? Direction.LEFT : Direction.RIGHT;

    return world
        .createEntity()
        .addComponent(new NetworkComponent(id, "BARREL"))
        .addComponent(pos)
        .addComponent(new VelocityComponent(velocity, 0))
        .addComponent(new BouncinessComponent())
        .addComponent(new GravityComponent(GRAVITY))
        .addComponent(new StateComponent(State.MOVING, direction))
        .addComponent(new DamageComponent(BARREL_DAMAGE))
        .addComponent(new CircleCollider(BARREL_COLLISION_RADIUS))
        .addComponent(
            new GraphicComponent(
                "/sprites/barrel.png",
                BARREL_WIDTH,
                BARREL_HEIGHT,
                BARREL_BORDER,
                BARREL_SCALE,
                BARREL_FRAME_DURATION,
                (state) -> new AnimationSettings(0, 0, 4)));
  }

  @Override
  public Entity createBarrel(double velocity) {
    PositionComponent pos = velocity < 0 ? LEFT_BARREL_SPAWN : RIGHT_BARREL_SPAWN;
    String uniqueId = java.util.UUID.randomUUID().toString();

    return buildBarrelEntity(uniqueId, pos, velocity);
  }

  @Override
  public Entity createNetworkBarrel(String id, PositionComponent position, double velocity) {
    return buildBarrelEntity(id, position, velocity);
  }

  @Override
  public Entity createPlatform(PositionComponent pos, RectangleCollider collider) {
    return world
        .createEntity()
        .addComponent(pos)
        .addComponent(new SolidComponent())
        .addComponent(collider);
  }

  @Override
  public Entity createLadder(PositionComponent pos, RectangleCollider collider) {
    return world
        .createEntity()
        .addComponent(pos)
        .addComponent(new ClimbableComponent())
        .addComponent(collider);
  }
}
