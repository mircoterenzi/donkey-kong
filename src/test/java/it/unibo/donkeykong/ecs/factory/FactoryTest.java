package it.unibo.donkeykong.ecs.factory;

import static it.unibo.donkeykong.core.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

import it.unibo.donkeykong.core.WorldImpl;
import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.StateComponent.*;
import it.unibo.donkeykong.ecs.component.api.Component;
import it.unibo.donkeykong.ecs.entity.EntityFactoryImpl;
import it.unibo.donkeykong.ecs.entity.api.Entity;
import it.unibo.donkeykong.ecs.entity.api.EntityFactory;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FactoryTest {

  private World world;
  private EntityFactory entityFactory;

  @BeforeEach
  void setUp() {
    world = new WorldImpl();
    entityFactory = new EntityFactoryImpl(world);
  }

  private <C extends Component> void assertComponentPresence(
      Entity entity, Class<C> componentClass, C expectedComponent) {
    assertTrue(
        entity.getComponent(componentClass).isPresent(),
        "Component " + componentClass.getSimpleName() + " should be present.");
    assertEquals(
        expectedComponent,
        entity.getComponent(componentClass).get(),
        "Component " + componentClass.getSimpleName() + " has wrong values.");
  }

  private <C extends Component> void assertComponentPresence(
      Entity entity, Class<C> componentClass) {
    assertTrue(
        entity.getComponent(componentClass).isPresent(),
        "Component " + componentClass.getSimpleName() + " should be present.");
  }

  @Test
  void testCreateFirstPlayer() {
    Entity player = entityFactory.createFirstPlayer();
    assertNotNull(player);
    assertComponentPresence(player, PositionComponent.class, FIRST_PLAYER_SPAWN);
    assertComponentPresence(player, VelocityComponent.class, new VelocityComponent(0, 0));
    assertComponentPresence(player, GravityComponent.class, new GravityComponent(GRAVITY));
    assertComponentPresence(player, HealthComponent.class, new HealthComponent(PLAYER_LIVES));
    assertComponentPresence(player, InputComponent.class);
    assertComponentPresence(
        player, StateComponent.class, new StateComponent(State.IDLE, Direction.RIGHT));
    assertComponentPresence(
        player,
        RectangleCollider.class,
        new RectangleCollider(PLAYER_COLLISION_WIDTH, PLAYER_COLLISION_HEIGHT));
    assertTrue(
        player.getComponent(GraphicComponent.class).isPresent(),
        "Player1 must have a GraphicComponent component");
    GraphicComponent actualGraphic = player.getComponent(GraphicComponent.class).get();
    assertEquals("/sprites/mario.png", actualGraphic.path());
    assertEquals(PLAYER_WIDTH, actualGraphic.width());
    assertEquals(PLAYER_HEIGHT, actualGraphic.height());
    assertEquals(PLAYER_BORDER, actualGraphic.border());
    assertEquals(PLAYER_SCALE, actualGraphic.scale());
    assertEquals(PLAYER_FRAME_DURATION, actualGraphic.frameDuration());
    Function<State, GraphicComponent.AnimationSettings> animationLogic =
        actualGraphic.stateToAnimationSettings();
    assertNotNull(animationLogic, "Animation logic cannot be null");
  }

  @Test
  void testCreateSecondPlayer() {
    Entity player = entityFactory.createSecondPlayer();
    assertNotNull(player);
    assertComponentPresence(player, PositionComponent.class, SECOND_PLAYER_SPAWN);
    assertComponentPresence(
        player, StateComponent.class, new StateComponent(State.IDLE, Direction.RIGHT));
    assertTrue(
        player.getComponent(GraphicComponent.class).isPresent(),
        "Player2 must have a GraphicComponent component");
    GraphicComponent actualGraphic = player.getComponent(GraphicComponent.class).get();
    assertEquals("/sprites/luigi.png", actualGraphic.path());
    assertEquals(PLAYER_WIDTH, actualGraphic.width());
    assertEquals(PLAYER_HEIGHT, actualGraphic.height());
    assertEquals(PLAYER_BORDER, actualGraphic.border());
    assertEquals(PLAYER_SCALE, actualGraphic.scale());
    assertEquals(PLAYER_FRAME_DURATION, actualGraphic.frameDuration());
    Function<State, GraphicComponent.AnimationSettings> animationLogic =
        actualGraphic.stateToAnimationSettings();
    assertNotNull(animationLogic, "Animation logic cannot be null");
  }

  @Test
  void testCreatePauline() {
    Entity pauline = entityFactory.createPauline();
    assertNotNull(pauline);
    assertComponentPresence(pauline, PositionComponent.class, PAULINE_POSITION);
    assertComponentPresence(
        pauline,
        RectangleCollider.class,
        new RectangleCollider(PAULINE_COLLISION_WIDTH, PAULINE_COLLISION_HEIGHT));
    assertTrue(
        pauline.getComponent(GraphicComponent.class).isPresent(),
        "Pauline deve avere un GraphicComponent component");
    GraphicComponent actualGraphic = pauline.getComponent(GraphicComponent.class).get();
    assertEquals("/sprites/pauline.png", actualGraphic.path());
    assertEquals(PAULINE_WIDTH, actualGraphic.width());
    assertEquals(PAULINE_HEIGHT, actualGraphic.height());
    assertEquals(PAULINE_BORDER, actualGraphic.border());
    assertEquals(PAULINE_SCALE, actualGraphic.scale());
    assertEquals(PAULINE_FRAME_DURATION, actualGraphic.frameDuration());
    Function<State, GraphicComponent.AnimationSettings> animationLogic =
        actualGraphic.stateToAnimationSettings();
    assertNotNull(animationLogic, "La logica di animazione non può essere null");
  }

  @Test
  void testCreateDonkeyKong() {
    Entity dk = entityFactory.createDonkeyKong();
    assertNotNull(dk);
    assertComponentPresence(dk, PositionComponent.class, DK_POSITION);
    assertComponentPresence(
        dk,
        RectangleCollider.class,
        new RectangleCollider(DK_COLLISION_WIDTH, DK_COLLISION_HEIGHT));
    assertTrue(
        dk.getComponent(GraphicComponent.class).isPresent(),
        "DonkeyKong must have a GraphicComponent component");
    GraphicComponent actualGraphic = dk.getComponent(GraphicComponent.class).get();
    assertEquals("/sprites/donkey.png", actualGraphic.path());
    assertEquals(DK_WIDTH, actualGraphic.width());
    assertEquals(DK_HEIGHT, actualGraphic.height());
    assertEquals(DK_BORDER, actualGraphic.border());
    assertEquals(DK_SCALE, actualGraphic.scale());
    assertEquals(DK_FRAME_DURATION, actualGraphic.frameDuration());
    Function<State, GraphicComponent.AnimationSettings> animationLogic =
        actualGraphic.stateToAnimationSettings();
    assertNotNull(animationLogic, "Animation logic cannot be null");
  }

  @Test
  void testCreateBarrel() {
    Entity barrel = entityFactory.createBarrel(BARREL_VELOCITY);

    assertNotNull(barrel);
    assertComponentPresence(barrel, PositionComponent.class, RIGHT_BARREL_SPAWN);
    assertComponentPresence(
        barrel, VelocityComponent.class, new VelocityComponent(BARREL_VELOCITY, 0));
    assertComponentPresence(barrel, BouncinessComponent.class);
    assertComponentPresence(barrel, GravityComponent.class, new GravityComponent(GRAVITY));
    assertComponentPresence(
        barrel, StateComponent.class, new StateComponent(State.MOVING, Direction.RIGHT));
    assertComponentPresence(
        barrel, CircleCollider.class, new CircleCollider(BARREL_COLLISION_RADIUS));
    assertTrue(
        barrel.getComponent(GraphicComponent.class).isPresent(),
        "Barrel must have a GraphicComponent component");
    GraphicComponent actualGraphic = barrel.getComponent(GraphicComponent.class).get();
    assertEquals("/sprites/barrel.png", actualGraphic.path());
    assertEquals(BARREL_WIDTH, actualGraphic.width());
    assertEquals(BARREL_HEIGHT, actualGraphic.height());
    assertEquals(BARREL_BORDER, actualGraphic.border());
    assertEquals(BARREL_SCALE, actualGraphic.scale());
    assertEquals(BARREL_FRAME_DURATION, actualGraphic.frameDuration());
    Function<State, GraphicComponent.AnimationSettings> animationLogic =
        actualGraphic.stateToAnimationSettings();
    assertNotNull(animationLogic, "Animation logic cannot be null");
  }

  @Test
  void testCreatePlatform() {
    PositionComponent testPos = new PositionComponent(50, 50);
    RectangleCollider testCollider = new RectangleCollider(100, 20);
    Entity platform = entityFactory.createPlatform(testPos, testCollider);

    assertNotNull(platform);
    assertComponentPresence(platform, PositionComponent.class, testPos);
    assertComponentPresence(platform, SolidComponent.class);
    assertComponentPresence(platform, RectangleCollider.class, testCollider);
  }

  @Test
  void testCreateLadder() {
    PositionComponent testPos = new PositionComponent(30, 60);
    RectangleCollider testCollider = new RectangleCollider(10, 80);
    Entity ladder = entityFactory.createLadder(testPos, testCollider);

    assertNotNull(ladder);
    assertComponentPresence(ladder, PositionComponent.class, testPos);
    assertComponentPresence(ladder, ClimbableComponent.class);
    assertComponentPresence(ladder, RectangleCollider.class, testCollider);
  }
}
