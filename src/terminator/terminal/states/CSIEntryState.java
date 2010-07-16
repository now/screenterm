package terminator.terminal.states;

import terminator.terminal.*;
import terminator.terminal.actions.*;

public class CSIEntryState extends State {
  private static CSIEntryState instance = new CSIEntryState();

  public static State enter() {
    return instance;
  }

  public State process(ActionQueue actions, char c) {
    if ((0x20 <= c && c <= 0x2f) || c == 0x3a)
      return CSIIgnoreState.enter();
    if (0x30 <= c && c <= 0x3f)
      return CSIParameterState.enter(actions, c);
    if (0x40 <= c && c <= 0x7e)
      return CSIParameterState.enter().process(actions, c);
    return super.process(actions, c);
  }
}
