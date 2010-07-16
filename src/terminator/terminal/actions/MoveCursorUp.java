package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class MoveCursorUp implements TerminalAction {
  private int count;

  public MoveCursorUp(int count) {
    this.count = count;
  }

  public void perform(TerminalModelModifier model) {
    model.moveCursorVertically(-count);
  }

  public String toString() {
    return "Move cursor up " + count + " rows";
  }
}
