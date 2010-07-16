package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class MoveCursorDown implements TerminalAction {
  private int count;

  public MoveCursorDown(int count) {
    this.count = count;
  }

  public void perform(TerminalModelModifier model) {
    model.moveCursorVertically(count);
  }

  public String toString() {
    return "Move cursor down " + count + " rows";
  }
}
