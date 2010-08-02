package terminator.terminal.states;

import terminator.terminal.*;
import terminator.terminal.actions.*;

public class EscapeState extends State {
  private static EscapeState instance = new EscapeState();

  public static State enter() {
    return instance;
  }

  @Override public State process(ActionQueue actions, char c) {
    switch (c) {
    case 'M':
      actions.add(new ReverseLineFeed());
      return GroundState.enter();
    case '[':
      return CSIEntryState.enter();
    }
    if (0x20 <= c && c <= 0x2f)
      return EscapeIntermediateState.enter();
    if (0x30 <= c && c <= 0x7e)
      return GroundState.enter();
    return super.process(actions, c);
  }
}
