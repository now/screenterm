package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class CarriageReturn implements TerminalAction {
  public void perform(TerminalModelModifier model) {
    model.carriageReturn();
  }

  public String toString() {
    return "Carriage return";
  }
}
