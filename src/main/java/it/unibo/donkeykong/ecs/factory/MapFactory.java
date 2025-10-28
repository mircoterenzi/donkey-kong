package it.unibo.donkeykong.ecs.factory;

import it.unibo.donkeykong.ecs.component.Position;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class responsible for generating the game map by creating platforms and ladders at
 * specified positions using the provided EntityFactory.
 */
public class MapFactory {
  private final EntityFactory entityFactory;
  private final Map<Position, String> positions;

  public MapFactory(final EntityFactory entityFactory) {
    this.entityFactory = entityFactory;
    this.positions = new HashMap<>();
    populatePositions();
  }

  public void generateMap() {
    positions.forEach(
        (key, value) -> {
          if (value.equals("platform")) {
            entityFactory.createPlatform(key);
          } else if (value.equals("ladder")) {
            entityFactory.createLadder(key);
          }
        });
  }

  private void populatePositions() {
    positions.put(new Position(1, 1), "platform");
    positions.put(new Position(2, 1), "ladder");
  }
}
