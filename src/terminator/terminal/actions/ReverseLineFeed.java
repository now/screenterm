package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class ReverseLineFeed implements TerminalAction {
  public void perform(TerminalModelModifier model) {
    model.reverseLineFeed();
  }

  public String toString() {
    return "Reverse line feed";
  }
}
