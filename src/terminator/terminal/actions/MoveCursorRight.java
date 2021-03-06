package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class MoveCursorRight implements TerminalAction {
  private int count;

  public MoveCursorRight(int count) {
    this.count = count;
  }

  public void perform(TerminalModelModifier model) {
    model.moveCursorHorizontally(count);
  }

  public String toString() {
    return "Move cursor right " + count + " columns";
  }
}
