package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class SetScrollingRegion implements TerminalAction {
  private int top;
  private int bottom;

  public SetScrollingRegion(int top, int bottom) {
    this.top = top;
    this.bottom = bottom;
  }

  public void perform(TerminalModelModifier model) {
    model.setScrollingRegion(top, bottom);
  }

  public String toString() {
    return "Set scrolling region between rows " + top + " and " + bottom;
  }
}
