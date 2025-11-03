package it.unibo.donkeykong.ecs.factory;

import it.unibo.donkeykong.ecs.component.Position;
import it.unibo.donkeykong.ecs.component.RectangleCollider;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class responsible for generating the game map by creating platforms and ladders at
 * specified positions using the provided EntityFactory.
 */
public class MapFactory {
  private final EntityFactory entityFactory;
  private final Map<Position, RectangleCollider> platforms;
  private final Map<Position, RectangleCollider> ladders;

  public MapFactory(final EntityFactory entityFactory) {
    this.entityFactory = entityFactory;
    this.platforms = new HashMap<>();
    this.ladders = new HashMap<>();
    populatePositions();
  }

  public void generateMap() {
    platforms.forEach(entityFactory::createPlatform);
    ladders.forEach(entityFactory::createLadder);
  }

  private void populatePositions() {
    platforms.put(new Position(448, 1008), new RectangleCollider(894, 29));
    platforms.put(new Position(448, 848), new RectangleCollider(765, 29));
    platforms.put(new Position(432, 688), new RectangleCollider(350, 29));
    platforms.put(new Position(752, 688), new RectangleCollider(157, 29));
    platforms.put(new Position(126, 688), new RectangleCollider(129, 29));
    platforms.put(new Position(192, 528), new RectangleCollider(381, 29));
    platforms.put(new Position(703, 528), new RectangleCollider(380, 29));
    platforms.put(new Position(448, 368), new RectangleCollider(765, 29));
    platforms.put(new Position(448, 240), new RectangleCollider(189, 29));
    ladders.put(new Position(112, 912), new RectangleCollider(30, 158));
    ladders.put(new Position(336, 912), new RectangleCollider(30, 158));
    ladders.put(new Position(560, 912), new RectangleCollider(30, 158));
    ladders.put(new Position(784, 912), new RectangleCollider(30, 158));
    ladders.put(new Position(592, 752), new RectangleCollider(30, 158));
    ladders.put(new Position(272, 752), new RectangleCollider(30, 158));
    ladders.put(new Position(112, 592), new RectangleCollider(30, 158));
    ladders.put(new Position(336, 592), new RectangleCollider(30, 158));
    ladders.put(new Position(560, 592), new RectangleCollider(30, 158));
    ladders.put(new Position(784, 592), new RectangleCollider(30, 158));
    ladders.put(new Position(80, 432), new RectangleCollider(30, 158));
    ladders.put(new Position(816, 432), new RectangleCollider(30, 158));
    ladders.put(new Position(272, 240), new RectangleCollider(30, 222));
    ladders.put(new Position(336, 240), new RectangleCollider(30, 222));
    ladders.put(new Position(528, 288), new RectangleCollider(30, 128));
  }
}
