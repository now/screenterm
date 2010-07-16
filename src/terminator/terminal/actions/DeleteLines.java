package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class DeleteLines implements TerminalAction {
  private int count;

  public DeleteLines(int count) {
    this.count = count;
  }

  public void perform(TerminalModelModifier model) {
    model.deleteLines(count);
  }

  public String toString() {
    return "Delete " + count + " lines";
  }
}
