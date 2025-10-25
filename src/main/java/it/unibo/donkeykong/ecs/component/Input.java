package it.unibo.donkeykong.ecs.component;

/** Input component, which represents the user input for an entity. */
public class Input implements Component {
  /** Possible horizontal inputs. */
  public enum HorizontalInput {
    MOVE_LEFT,
    MOVE_RIGHT,
    NONE
  }

  /** Possible vertical inputs. */
  public enum VerticalInput {
    MOVE_UP,
    MOVE_DOWN,
    NONE
  }

  private HorizontalInput currentHInput;
  private VerticalInput currentVInput;

  private boolean jumpPressed;

  public Input() {
    this.currentHInput = HorizontalInput.NONE;
    this.currentVInput = VerticalInput.NONE;
    this.jumpPressed = false;
  }

  public HorizontalInput getCurrentHInput() {
    return currentHInput;
  }

  public void setCurrentHInput(final HorizontalInput currentHInput) {
    this.currentHInput = currentHInput;
  }

  public VerticalInput getCurrentVInput() {
    return currentVInput;
  }

  public void setCurrentVInput(final VerticalInput currentVInput) {
    this.currentVInput = currentVInput;
  }

  public boolean isJumpPressed() {
    return jumpPressed;
  }

  public void setJumpPressed(final boolean jumpPressed) {
    this.jumpPressed = jumpPressed;
  }
}
