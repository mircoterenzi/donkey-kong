package it.unibo.donkeykong.ecs.factory;

import static it.unibo.donkeykong.ecs.component.StateComponent.Direction.LEFT;
import static it.unibo.donkeykong.ecs.component.StateComponent.State.IDLE;
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
                64, 64, 100, new StateComponent(IDLE, LEFT), 0, Map.of(IDLE, List.of("player"))));
  }

  @Override
  public Entity createSecondPlayer() {
    return world.createEntity().addComponent(SECOND_PLAYER_SPAWN);
    // .addComponent(new Graphic()) quando sarà aggiornato
  }

  @Override
  public Entity createPauline() {
    return world.createEntity().addComponent(PAULINE_POSITION);
    // .addComponent(new Graphic()) quando sarà aggiornato
  }

  @Override
  public Entity createDonkeyKong() {
    return world.createEntity().addComponent(DK_POSITION);
    // .addComponent(new Graphic()) quando sarà aggiornato
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
        .addComponent(new CircleCollider(BARREL_COLLISION_RADIUS));
    // .addComponent(new Graphic()) quando sarà aggiornato
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
