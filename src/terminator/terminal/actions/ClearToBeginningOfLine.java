package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class ClearToBeginningOfLine implements TerminalAction {
  public void perform(TerminalModelModifier model) {
    model.clearToBeginningOfLine();
  }

  public String toString() {
    return "Clear to beginning of line";
  }
}
