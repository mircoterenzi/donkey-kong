package it.unibo.donkeykong.ecs.factory;

import static it.unibo.donkeykong.utilities.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.WorldImpl;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.StateComponent.*;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.List;
import java.util.Set;
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
    assertComponentPresence(player, Position.class, FIRST_PLAYER_SPAWN);
    assertComponentPresence(player, Velocity.class, new Velocity(0, 0));
    assertComponentPresence(player, Gravity.class, new Gravity(GRAVITY));
    assertComponentPresence(player, Health.class, new Health(PLAYER_LIVES));
    assertComponentPresence(player, Input.class);
    assertComponentPresence(
        player, StateComponent.class, new StateComponent(State.IDLE, Direction.RIGHT));
    assertComponentPresence(
        player, CircleCollider.class, new CircleCollider(PLAYER_COLLISION_RADIUS));
    assertTrue(
        player.getComponent(Graphic.class).isPresent(), "Player1 must have a Graphic component");
    Graphic actualGraphic = player.getComponent(Graphic.class).get();
    assertEquals("/sprites/mario.png", actualGraphic.path());
    assertEquals(PLAYER_WIDTH, actualGraphic.width());
    assertEquals(PLAYER_HEIGHT, actualGraphic.height());
    assertEquals(PLAYER_BORDER, actualGraphic.border());
    assertEquals(PLAYER_SCALE, actualGraphic.scale());
    assertEquals(PLAYER_FRAME_DURATION, actualGraphic.frameDuration());
    Function<State, Graphic.AnimationSettings> animationLogic =
        actualGraphic.stateToAnimationSettings();
    assertNotNull(animationLogic, "Animation logic cannot be null");
  }

  @Test
  void testCreateSecondPlayer() {
    Entity player = entityFactory.createSecondPlayer();
    assertNotNull(player);
    assertComponentPresence(player, Position.class, SECOND_PLAYER_SPAWN);
    assertComponentPresence(
        player, StateComponent.class, new StateComponent(State.IDLE, Direction.RIGHT));
    assertTrue(
        player.getComponent(Graphic.class).isPresent(), "Player2 must have a Graphic component");
    Graphic actualGraphic = player.getComponent(Graphic.class).get();
    assertEquals("/sprites/luigi.png", actualGraphic.path());
    assertEquals(PLAYER_WIDTH, actualGraphic.width());
    assertEquals(PLAYER_HEIGHT, actualGraphic.height());
    assertEquals(PLAYER_BORDER, actualGraphic.border());
    assertEquals(PLAYER_SCALE, actualGraphic.scale());
    assertEquals(PLAYER_FRAME_DURATION, actualGraphic.frameDuration());
    Function<State, Graphic.AnimationSettings> animationLogic =
        actualGraphic.stateToAnimationSettings();
    assertNotNull(animationLogic, "Animation logic cannot be null");
  }

  @Test
  void testCreatePauline() {
    Entity pauline = entityFactory.createPauline();
    assertNotNull(pauline);
    assertComponentPresence(pauline, Position.class, PAULINE_POSITION);
    assertComponentPresence(
        pauline, CircleCollider.class, new CircleCollider(PAULINE_COLLISION_RADIUS));
    assertTrue(
        pauline.getComponent(Graphic.class).isPresent(), "Pauline deve avere un Graphic component");
    Graphic actualGraphic = pauline.getComponent(Graphic.class).get();
    assertEquals("/sprites/pauline.png", actualGraphic.path());
    assertEquals(PAULINE_WIDTH, actualGraphic.width());
    assertEquals(PAULINE_HEIGHT, actualGraphic.height());
    assertEquals(PAULINE_BORDER, actualGraphic.border());
    assertEquals(PAULINE_SCALE, actualGraphic.scale());
    assertEquals(PAULINE_FRAME_DURATION, actualGraphic.frameDuration());
    Function<State, Graphic.AnimationSettings> animationLogic =
        actualGraphic.stateToAnimationSettings();
    assertNotNull(animationLogic, "La logica di animazione non può essere null");
  }

  @Test
  void testCreateDonkeyKong() {
    Entity dk = entityFactory.createDonkeyKong();
    assertNotNull(dk);
    assertComponentPresence(dk, Position.class, DK_POSITION);
    assertComponentPresence(dk, CircleCollider.class, new CircleCollider(DK_COLLISION_RADIUS));
    assertTrue(
        dk.getComponent(Graphic.class).isPresent(), "DonkeyKong must have a Graphic component");
    Graphic actualGraphic = dk.getComponent(Graphic.class).get();
    assertEquals("/sprites/donkey.png", actualGraphic.path());
    assertEquals(DK_WIDTH, actualGraphic.width());
    assertEquals(DK_HEIGHT, actualGraphic.height());
    assertEquals(DK_BORDER, actualGraphic.border());
    assertEquals(DK_SCALE, actualGraphic.scale());
    assertEquals(DK_FRAME_DURATION, actualGraphic.frameDuration());
    Function<State, Graphic.AnimationSettings> animationLogic =
        actualGraphic.stateToAnimationSettings();
    assertNotNull(animationLogic, "Animation logic cannot be null");
  }

  @Test
  void testCreateBarrel() {
    Position testPos = new Position(100, 100);
    Direction testDir = Direction.RIGHT;
    Entity barrel = entityFactory.createBarrel(testPos, testDir);

    assertNotNull(barrel);
    assertComponentPresence(barrel, Position.class, testPos);
    assertComponentPresence(barrel, Velocity.class, new Velocity(BARREL_VELOCITY, 0));
    assertComponentPresence(barrel, Bounciness.class);
    assertComponentPresence(barrel, Gravity.class, new Gravity(GRAVITY));
    assertComponentPresence(
        barrel, StateComponent.class, new StateComponent(State.MOVING, testDir));
    assertComponentPresence(
        barrel, CircleCollider.class, new CircleCollider(BARREL_COLLISION_RADIUS));
    assertTrue(
        barrel.getComponent(Graphic.class).isPresent(), "Barrel must have a Graphic component");
    Graphic actualGraphic = barrel.getComponent(Graphic.class).get();
    assertEquals("/sprites/barrel.png", actualGraphic.path());
    assertEquals(BARREL_WIDTH, actualGraphic.width());
    assertEquals(BARREL_HEIGHT, actualGraphic.height());
    assertEquals(BARREL_BORDER, actualGraphic.border());
    assertEquals(BARREL_SCALE, actualGraphic.scale());
    assertEquals(BARREL_FRAME_DURATION, actualGraphic.frameDuration());
    Function<State, Graphic.AnimationSettings> animationLogic =
        actualGraphic.stateToAnimationSettings();
    assertNotNull(animationLogic, "Animation logic cannot be null");
  }

  @Test
  void testCreatePlatform() {
    Position testPos = new Position(50, 50);
    RectangleCollider testCollider = new RectangleCollider(100, 20);
    Entity platform = entityFactory.createPlatform(testPos, testCollider);

    assertNotNull(platform);
    assertComponentPresence(platform, Position.class, testPos);
    assertComponentPresence(platform, GroundComponent.class);
    assertComponentPresence(platform, RectangleCollider.class, testCollider);
  }

  @Test
  void testCreateLadder() {
    Position testPos = new Position(30, 60);
    RectangleCollider testCollider = new RectangleCollider(10, 80);
    Entity ladder = entityFactory.createLadder(testPos, testCollider);

    assertNotNull(ladder);
    assertComponentPresence(ladder, Position.class, testPos);
    assertComponentPresence(ladder, Climbable.class);
    assertComponentPresence(ladder, RectangleCollider.class, testCollider);
  }

  @Test
  void testMapFactoryGenerateMap() {
    MapFactory mapFactory = new MapFactory(entityFactory);
    mapFactory.generateMap();

    Set<Entity> platforms = world.getEntitiesWithComponents(List.of(GroundComponent.class));
    Set<Entity> ladders = world.getEntitiesWithComponents(List.of(Climbable.class));

    Position expectedPlatformPos = new Position(448, 1008);
    RectangleCollider expectedPlatformCollider = new RectangleCollider(894, 29);
    Position expectedLadderPos = new Position(112, 928);
    RectangleCollider expectedLadderCollider = new RectangleCollider(30, 128);

    assertTrue(
        platforms.stream()
            .anyMatch(
                e -> e.getComponent(Position.class).orElseThrow().equals(expectedPlatformPos)),
        "Nessuna piattaforma trovata alla posizione " + expectedPlatformPos);
    assertTrue(
        ladders.stream()
            .anyMatch(e -> e.getComponent(Position.class).orElseThrow().equals(expectedLadderPos)),
        "Nessuna scala trovata alla posizione " + expectedLadderPos);

    Entity platform =
        platforms.stream()
            .filter(e -> e.getComponent(Position.class).orElseThrow().equals(expectedPlatformPos))
            .findFirst()
            .orElseThrow();
    Entity ladder =
        ladders.stream()
            .filter(e -> e.getComponent(Position.class).orElseThrow().equals(expectedLadderPos))
            .findFirst()
            .orElseThrow();

    assertComponentPresence(platform, RectangleCollider.class, expectedPlatformCollider);
    assertComponentPresence(ladder, RectangleCollider.class, expectedLadderCollider);
  }
}
