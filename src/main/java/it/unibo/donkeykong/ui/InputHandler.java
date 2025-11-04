package it.unibo.donkeykong.ui;

import static it.unibo.donkeykong.ecs.component.InputComponent.HorizontalInput.*;
import static it.unibo.donkeykong.ecs.component.InputComponent.VerticalInput.*;

import it.unibo.donkeykong.core.api.World;
import it.unibo.donkeykong.ecs.component.InputComponent;
import java.util.List;
import java.util.function.Consumer;
import javafx.scene.input.KeyCode;

/**
 * InputHandler is responsible for handling keyboard input events and updating the InputComponent
 * components of entities in the game world accordingly.
 */
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
    final InputComponent.HorizontalInput hInput;
    if (leftPressed) {
      hInput = MOVE_LEFT;
    } else if (rightPressed) {
      hInput = MOVE_RIGHT;
    } else {
      hInput = InputComponent.HorizontalInput.NONE;
    }

    final InputComponent.VerticalInput vInput;
    if (upPressed) {
      vInput = MOVE_UP;
    } else if (downPressed) {
      vInput = MOVE_DOWN;
    } else {
      vInput = InputComponent.VerticalInput.NONE;
    }

    applyToInput(
        i -> {
          i.setCurrentHInput(hInput);
          i.setCurrentVInput(vInput);
        });
  }

  private void applyToInput(final Consumer<InputComponent> inputLogic) {
    world
        .getEntitiesWithComponents(List.of(InputComponent.class))
        .forEach(e -> e.getComponent(InputComponent.class).ifPresent(inputLogic));
  }
}
