package it.unibo.donkeykong.utilities;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.Input;
import javafx.scene.input.KeyCode;

import java.util.List;

import static it.unibo.donkeykong.ecs.component.Input.HorizontalInput.*;
import static it.unibo.donkeykong.ecs.component.Input.VerticalInput.*;

public record InputHandler(World world) {

  public void handleKeyPress(KeyCode keyCode) {
    switch (keyCode) {
      case LEFT:
      case A:
        world.getEntitiesWithComponents(List.of(Input.class)).forEach(e ->
          e.getComponent(Input.class).ifPresent(i -> i.setCurrentHInput(MOVE_LEFT)));
        break;
      case RIGHT:
      case D:
        world.getEntitiesWithComponents(List.of(Input.class)).forEach(e ->
          e.getComponent(Input.class).ifPresent(i -> i.setCurrentHInput(MOVE_RIGHT)));
        break;
      case UP:
      case W:
        world.getEntitiesWithComponents(List.of(Input.class)).forEach(e ->
          e.getComponent(Input.class).ifPresent(i -> i.setCurrentVInput(MOVE_UP)));
        break;
      case DOWN:
      case S:
        world.getEntitiesWithComponents(List.of(Input.class)).forEach(e ->
          e.getComponent(Input.class).ifPresent(i -> i.setCurrentVInput(MOVE_DOWN)));
        break;
      case SPACE:
        world.getEntitiesWithComponents(List.of(Input.class)).forEach(e ->
          e.getComponent(Input.class).ifPresent(i -> i.setJumpPressed(true)));
        break;
      default:
        break;
    }
  }

  public void handleKeyRelease(KeyCode keyCode) {
    switch (keyCode) {
      case LEFT:
      case A:
      case RIGHT:
      case D:
        world.getEntitiesWithComponents(List.of(Input.class)).forEach(e ->
          e.getComponent(Input.class).ifPresent(i -> i.setCurrentHInput(Input.HorizontalInput.NONE)));
        break;
      case UP:
      case W:
      case DOWN:
      case S:
        world.getEntitiesWithComponents(List.of(Input.class)).forEach(e ->
          e.getComponent(Input.class).ifPresent(i -> i.setCurrentVInput(Input.VerticalInput.NONE)));
        break;
      default:
        break;
    }
  }
}
