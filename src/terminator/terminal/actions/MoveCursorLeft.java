package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class MoveCursorLeft implements TerminalAction {
  private int count;

  public MoveCursorLeft(int count) {
    this.count = count;
  }

  public void perform(TerminalModelModifier model) {
    model.moveCursorHorizontally(-count);
  }

  public String toString() {
    return "Move cursor left " + count + " columns";
  }
}
