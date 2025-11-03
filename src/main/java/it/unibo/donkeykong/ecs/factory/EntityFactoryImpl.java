package it.unibo.donkeykong.ecs.factory;

import static it.unibo.donkeykong.utilities.Constants.*;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.StateComponent.*;
import it.unibo.donkeykong.ecs.entity.Entity;

/** Implementation of the EntityFactory interface responsible for creating various game entities. */
public record EntityFactoryImpl(World world) implements EntityFactory {

  @Override
  public Entity createFirstPlayer() {
    return world
        .createEntity()
        .addComponent(FIRST_PLAYER_SPAWN)
        .addComponent(new Input())
        .addComponent(new Gravity(GRAVITY))
        .addComponent(new Velocity(0, 0))
        .addComponent(new Health(PLAYER_LIVES))
        .addComponent(new StateComponent(State.IDLE, Direction.RIGHT))
        .addComponent(new CircleCollider(PLAYER_COLLISION_RADIUS))
        .addComponent(
            new Graphic(
                "/sprites/mario.png",
                PLAYER_WIDTH,
                PLAYER_HEIGHT,
                PLAYER_BORDER,
                PLAYER_SCALE,
                PLAYER_FRAME_DURATION,
                (state) -> {
                  if (state == State.MOVING) {
                    return new Graphic.AnimationSettings(1, 0, 2);
                  }
                  return new Graphic.AnimationSettings(0, 0, 1);
                }));
  }

  @Override
  public Entity createSecondPlayer() {
    return world
        .createEntity()
        .addComponent(SECOND_PLAYER_SPAWN)
        .addComponent(new StateComponent(State.IDLE, Direction.RIGHT))
        .addComponent(
            new Graphic(
                "/sprites/luigi.png",
                PLAYER_WIDTH,
                PLAYER_HEIGHT,
                PLAYER_BORDER,
                PLAYER_SCALE,
                PLAYER_FRAME_DURATION,
                (state) -> {
                  if (state == State.MOVING) {
                    return new Graphic.AnimationSettings(1, 0, 2);
                  }
                  return new Graphic.AnimationSettings(0, 0, 1);
                }));
  }

  @Override
  public Entity createPauline() {
    return world
        .createEntity()
        .addComponent(PAULINE_POSITION)
        .addComponent(new CircleCollider(PAULINE_COLLISION_RADIUS))
        .addComponent(
            new Graphic(
                "/sprites/pauline.png",
                PAULINE_WIDTH,
                PAULINE_HEIGHT,
                PAULINE_BORDER,
                PAULINE_SCALE,
                PAULINE_FRAME_DURATION,
                (state) -> {
                  if (state == State.MOVING) {
                    return new Graphic.AnimationSettings(1, 0, 2);
                  }
                  return new Graphic.AnimationSettings(0, 0, 1);
                }));
  }

  @Override
  public Entity createDonkeyKong() {
    return world
        .createEntity()
        .addComponent(DK_POSITION)
        .addComponent(new CircleCollider(DK_COLLISION_RADIUS))
        .addComponent(
            new Graphic(
                "/sprites/donkey.png",
                DK_WIDTH,
                DK_HEIGHT,
                DK_BORDER,
                DK_SCALE,
                DK_FRAME_DURATION,
                (state) -> {
                  if (state == State.MOVING) {
                    return new Graphic.AnimationSettings(1, 0, 2);
                  }
                  return new Graphic.AnimationSettings(0, 0, 1);
                }));
  }

  @Override
  public Entity createBarrel(Position pos, Direction direction) {
    return world
        .createEntity()
        .addComponent(pos)
        .addComponent(new Velocity(BARREL_VELOCITY, 0))
        .addComponent(new Bounciness())
        .addComponent(new Gravity(GRAVITY))
        .addComponent(new StateComponent(State.MOVING, direction))
        .addComponent(new CircleCollider(BARREL_COLLISION_RADIUS))
        .addComponent(
            new Graphic(
                "/sprites/barrel.png",
                BARREL_WIDTH,
                BARREL_HEIGHT,
                BARREL_BORDER,
                BARREL_SCALE,
                BARREL_FRAME_DURATION,
                (state) -> {
                  return new Graphic.AnimationSettings(0, 0, 4);
                }));
  }

  @Override
  public Entity createPlatform(Position pos, RectangleCollider collider) {
    return world
        .createEntity()
        .addComponent(pos)
        .addComponent(new SolidComponent())
        .addComponent(collider);
  }

  @Override
  public Entity createLadder(Position pos, RectangleCollider collider) {
    return world
        .createEntity()
        .addComponent(pos)
        .addComponent(new Climbable())
        .addComponent(collider);
  }
}
