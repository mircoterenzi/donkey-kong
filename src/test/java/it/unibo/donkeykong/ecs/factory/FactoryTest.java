package it.unibo.donkeykong.ecs.factory;

import static it.unibo.donkeykong.utilities.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.WorldImpl;
import it.unibo.donkeykong.ecs.component.*;
import it.unibo.donkeykong.ecs.component.StateComponent.Direction;
import it.unibo.donkeykong.ecs.component.StateComponent.State;
import it.unibo.donkeykong.ecs.entity.Entity;
import java.util.List;
import java.util.Set;
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
    assertComponentPresence(player, Velocity.class, new Velocity(PLAYER_VELOCITY, 0));
    assertComponentPresence(player, Gravity.class, new Gravity(GRAVITY));
    assertComponentPresence(player, Health.class, new Health(PLAYER_LIVES));
    assertComponentPresence(player, Input.class);
    assertComponentPresence(player, StateComponent.class, new StateComponent(State.IDLE, Direction.LEFT));
    assertComponentPresence(player, CircleCollider.class, new CircleCollider(PLAYER_COLLISION_RADIUS));
  }

  @Test
  void testCreateSecondPlayer() {
    Entity player = entityFactory.createSecondPlayer();
    assertNotNull(player);
    assertComponentPresence(player, Position.class, SECOND_PLAYER_SPAWN);
  }

  @Test
  void testCreatePauline() {
    Entity pauline = entityFactory.createPauline();
    assertNotNull(pauline);
    assertComponentPresence(pauline, Position.class, PAULINE_POSITION);
  }

  @Test
  void testCreateDonkeyKong() {
    Entity dk = entityFactory.createDonkeyKong();
    assertNotNull(dk);
    assertComponentPresence(dk, Position.class, DK_POSITION);
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
    assertComponentPresence(barrel, StateComponent.class, new StateComponent(State.MOVING, testDir));
    assertComponentPresence(barrel, CircleCollider.class, new CircleCollider(BARREL_COLLISION_RADIUS));
  }

  @Test
  void testCreatePlatform() {
    Position testPos = new Position(50, 50);
    Entity platform = entityFactory.createPlatform(testPos);

    assertNotNull(platform);
    assertComponentPresence(platform, Position.class, testPos);
    assertComponentPresence(platform, GroundComponent.class);
    assertComponentPresence(
      platform, RectangleCollider.class, new RectangleCollider(BLOCKS_COLLISION, BLOCKS_COLLISION));
  }

  @Test
  void testCreateLadder() {
    Position testPos = new Position(30, 60);
    Entity ladder = entityFactory.createLadder(testPos);

    assertNotNull(ladder);
    assertComponentPresence(ladder, Position.class, testPos);
    assertComponentPresence(ladder, Climbable.class);
    assertComponentPresence(
      ladder, RectangleCollider.class, new RectangleCollider(BLOCKS_COLLISION, BLOCKS_COLLISION));
  }

  @Test
  void testMapFactoryGenerateMap() {
    MapFactory mapFactory = new MapFactory(entityFactory);
    mapFactory.generateMap();

    Set<Entity> platforms = world.getEntitiesWithComponents(List.of(GroundComponent.class));
    Set<Entity> ladders = world.getEntitiesWithComponents(List.of(Climbable.class));

    // TODO: andrà cambiato in base alla mappa che si crea nel MapFactory
    Position expectedPlatformPos = new Position(1, 1);
    Position expectedLadderPos = new Position(2, 1);

    assertTrue(
      platforms.stream()
        .anyMatch(
          e -> e.getComponent(Position.class).orElseThrow().equals(expectedPlatformPos)),
      "Nessuna piattaforma trovata alla posizione " + expectedPlatformPos);
    assertTrue(
      ladders.stream()
        .anyMatch(e -> e.getComponent(Position.class).orElseThrow().equals(expectedLadderPos)),
      "Nessuna scala trovata alla posizione " + expectedLadderPos);

    Entity platform = platforms.stream()
      .filter(e -> e.getComponent(Position.class).orElseThrow().equals(expectedPlatformPos))
      .findFirst()
      .orElseThrow();
    Entity ladder = ladders.stream()
      .filter(e -> e.getComponent(Position.class).orElseThrow().equals(expectedLadderPos))
      .findFirst()
      .orElseThrow();

    assertComponentPresence(
      platform, RectangleCollider.class, new RectangleCollider(BLOCKS_COLLISION, BLOCKS_COLLISION));
    assertComponentPresence(
      ladder, RectangleCollider.class, new RectangleCollider(BLOCKS_COLLISION, BLOCKS_COLLISION));
  }
}
