package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class ClearToEndOfLine implements TerminalAction {
  public void perform(TerminalModelModifier model) {
    model.clearToEndOfLine();
  }

  public String toString() {
    return "Clear to end of line";
  }
}
