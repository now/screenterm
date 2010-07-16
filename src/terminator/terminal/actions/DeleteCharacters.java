package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class DeleteCharacters implements TerminalAction {
  private int count;

  public DeleteCharacters(int count) {
    this.count = count;
  }

  public void perform(TerminalModelModifier model) {
    model.deleteCharacters(count);
  }

  public String toString() {
    return "Delete " + count + " characters";
  }
}
