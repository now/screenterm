package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class SetCursorVisible implements TerminalAction {
  private boolean value;

  public SetCursorVisible(boolean value) {
    this.value = value;
  }

  public void perform(TerminalModelModifier model) {
    model.setCursorVisible(value);
  }

  public String toString() {
    return (value ? "Show" : "Hide") + " cursor";
  }
}
