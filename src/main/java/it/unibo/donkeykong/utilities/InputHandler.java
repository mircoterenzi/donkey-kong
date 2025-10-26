package it.unibo.donkeykong.utilities;

import it.unibo.donkeykong.ecs.World;
import it.unibo.donkeykong.ecs.component.Input;
import javafx.scene.input.KeyCode;

import java.util.List;
import java.util.function.Consumer;

import static it.unibo.donkeykong.ecs.component.Input.HorizontalInput.*;
import static it.unibo.donkeykong.ecs.component.Input.VerticalInput.*;

public class InputHandler {
  private final World world;
  private boolean leftPressed, rightPressed, upPressed, downPressed;

  public InputHandler(final World world) {
    this.world = world;
  }

  public void handleKeyEvent(final KeyCode keyCode, final boolean isPressed) {
    boolean stateChanged = false;

    switch (keyCode) {
      case LEFT:
      case A:
        leftPressed = isPressed;
        stateChanged = true;
        break;
      case RIGHT:
      case D:
        rightPressed = isPressed;
        stateChanged = true;
        break;
      case UP:
      case W:
        upPressed = isPressed;
        stateChanged = true;
        break;
      case DOWN:
      case S:
        downPressed = isPressed;
        stateChanged = true;
        break;
      case SPACE:
        if (isPressed) {
          applyToInput(i -> i.setJumpPressed(true));
        }
        break;
      default:
        break;
    }

    if (stateChanged) {
      updateInputState();
    }
  }

  private void updateInputState() {
    final Input.HorizontalInput hInput;
    if (leftPressed) {
      hInput = MOVE_LEFT;
    } else if (rightPressed) {
      hInput = MOVE_RIGHT;
    } else {
      hInput = Input.HorizontalInput.NONE;
    }

    final Input.VerticalInput vInput;
    if (upPressed) {
      vInput = MOVE_UP;
    } else if (downPressed) {
      vInput = MOVE_DOWN;
    } else {
      vInput = Input.VerticalInput.NONE;
    }

    applyToInput(i -> {
      i.setCurrentHInput(hInput);
      i.setCurrentVInput(vInput);
    });
  }

  private void applyToInput(final Consumer<Input> inputLogic) {
    world
      .getEntitiesWithComponents(List.of(Input.class))
      .forEach(e -> e.getComponent(Input.class).ifPresent(inputLogic));
  }
}
