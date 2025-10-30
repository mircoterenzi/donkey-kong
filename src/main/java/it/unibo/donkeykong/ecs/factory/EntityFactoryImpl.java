package it.unibo.donkeykong.ecs.factory;

import static it.unibo.donkeykong.utilities.Constants.*;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.StateComponent.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.List;
import java.util.Map;

/** Implementation of the EntityFactory interface responsible for creating various game entities. */
public record EntityFactoryImpl(World world) implements EntityFactory {

  @Override
  public Entity createFirstPlayer() {
    return world
        .createEntity()
        .addComponent(FIRST_PLAYER_SPAWN)
        .addComponent(new Velocity(PLAYER_VELOCITY, 0))
        .addComponent(new Gravity(GRAVITY))
        .addComponent(new Health(PLAYER_LIVES))
        .addComponent(new Input())
        .addComponent(new StateComponent(State.IDLE, Direction.LEFT))
        .addComponent(new CircleCollider(PLAYER_COLLISION_RADIUS))
        .addComponent(
            new Graphic(
                PLAYER_WIDTH,
                PLAYER_HEIGHT,
                PLAYER_FRAME_DURATION,
                new StateComponent(State.IDLE, Direction.LEFT),
                0,
                Map.of(State.IDLE, List.of("player1"))));
  }

  @Override
  public Entity createSecondPlayer() {
    return world
        .createEntity()
        .addComponent(SECOND_PLAYER_SPAWN)
        .addComponent(
            new Graphic(
                PLAYER_WIDTH,
                PLAYER_HEIGHT,
                PLAYER_FRAME_DURATION,
                new StateComponent(State.IDLE, Direction.LEFT),
                0,
                Map.of(State.IDLE, List.of("player2"))));
  }

  @Override
  public Entity createPauline() {
    return world
        .createEntity()
        .addComponent(PAULINE_POSITION)
        .addComponent(new CircleCollider(PAULINE_COLLISION_RADIUS))
        .addComponent(
            new Graphic(
                PAULINE_WIDTH,
                PAULINE_HEIGHT,
                PAULINE_FRAME_DURATION,
                new StateComponent(State.IDLE, Direction.LEFT),
                0,
                Map.of(State.IDLE, List.of("pauline"))));
  }

  @Override
  public Entity createDonkeyKong() {
    return world
        .createEntity()
        .addComponent(DK_POSITION)
        .addComponent(new CircleCollider(DK_COLLISION_RADIUS))
        .addComponent(
            new Graphic(
                DK_WIDTH,
                DK_HEIGHT,
                DK_FRAME_DURATION,
                new StateComponent(State.IDLE, Direction.LEFT),
                0,
                Map.of(State.IDLE, List.of("dk"))));
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
                BARREL_WIDTH,
                BARREL_HEIGHT,
                BARREL_FRAME_DURATION,
                new StateComponent(State.MOVING, direction),
                0,
                Map.of(State.MOVING, List.of("barrel"))));
  }

  @Override
  public Entity createPlatform(Position pos, RectangleCollider collider) {
    return world
        .createEntity()
        .addComponent(pos)
        .addComponent(new GroundComponent())
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
