package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class SetInsertMode implements TerminalAction {
  private boolean value;

  public SetInsertMode(boolean value) {
    this.value = value;
  }

  public void perform(TerminalModelModifier model) {
    model.setInsertMode(value);
  }

  public String toString() {
    return (value ? "Enter" : "Exit") + " insert mode";
  }
}
