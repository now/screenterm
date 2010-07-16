package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class AddText implements TerminalAction {
  private String text;

  public AddText(String text) {
    this.text = text;
  }

  public void perform(TerminalModelModifier model) {
    model.addText(text);
  }

  public String toString() {
    return "Add text " + text;
  }
}
