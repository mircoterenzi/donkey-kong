package it.unibo.donkeykong.ecs.entity;

import static it.unibo.donkeykong.core.Constants.*;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.GraphicComponent.*;
import it.unibo.donkeykong.ecs.component.StateComponent.*;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.entity.api.EntityFactory;

/** Implementation of the EntityFactory interface responsible for creating various game entities. */
public record EntityFactoryImpl(World world) implements EntityFactory {

  @Override
  public Entity createFirstPlayer() {
    return world
        .createEntity()
        .addComponent(FIRST_PLAYER_SPAWN)
        .addComponent(new InputComponent())
        .addComponent(new GravityComponent(GRAVITY))
        .addComponent(new VelocityComponent(0, 0))
        .addComponent(new HealthComponent(PLAYER_LIVES))
        .addComponent(new StateComponent(State.IDLE, Direction.RIGHT))
        .addComponent(new RectangleCollider(PLAYER_COLLISION_WIDTH, PLAYER_COLLISION_HEIGHT))
        .addComponent(
            new GraphicComponent(
                "/sprites/mario.png",
                PLAYER_WIDTH,
                PLAYER_HEIGHT,
                PLAYER_BORDER,
                PLAYER_SCALE,
                PLAYER_FRAME_DURATION,
                (state) ->
                    switch (state) {
                      case MOVING -> new AnimationSettings(1, 0, 2);
                      case JUMP -> new AnimationSettings(3, 0, 1);
                      case FALL -> new AnimationSettings(4, 0, 1);
                      case UP, DOWN -> new AnimationSettings(5, 0, 2);
                      case STOP_CLIMB -> new AnimationSettings(5, 0, 1);
                      default -> new AnimationSettings(0, 0, 1);
                    }));
  }

  @Override
  public Entity createSecondPlayer() {
    return world
        .createEntity()
        .addComponent(SECOND_PLAYER_SPAWN)
        .addComponent(new StateComponent(State.IDLE, Direction.RIGHT))
        .addComponent(
            new GraphicComponent(
                "/sprites/luigi.png",
                PLAYER_WIDTH,
                PLAYER_HEIGHT,
                PLAYER_BORDER,
                PLAYER_SCALE,
                PLAYER_FRAME_DURATION,
                (state) -> {
                  if (state == State.MOVING) {
                    return new GraphicComponent.AnimationSettings(1, 0, 2);
                  }
                  return new GraphicComponent.AnimationSettings(0, 0, 1);
                }));
  }

  @Override
  public Entity createPauline() {
    return world
        .createEntity()
        .addComponent(PAULINE_POSITION)
        .addComponent(new RectangleCollider(PAULINE_COLLISION_WIDTH, PAULINE_COLLISION_HEIGHT))
        .addComponent(new StateComponent(State.IDLE, Direction.RIGHT))
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
                (state) -> {
                  if (state == State.MOVING) {
                    return new GraphicComponent.AnimationSettings(0, 0, 1);
                  }
                  return new GraphicComponent.AnimationSettings(0, 0, 1);
                }));
  }

  @Override
  public Entity createBarrel(PositionComponent pos, Direction direction) {
    return world
        .createEntity()
        .addComponent(pos)
        .addComponent(new VelocityComponent(BARREL_VELOCITY, 0))
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
