package terminator.terminal.states;

import terminator.terminal.*;

public class EscapeIntermediateState extends State {
  private static EscapeIntermediateState instance = new EscapeIntermediateState();

  public static State enter() {
    return instance;
  }

  public State process(ActionQueue actions, char c) {
    if (0x20 <= c && c <= 0x2f)
      return this;
    if (0x30 <= c && c <= 0x7e)
      return GroundState.enter();
    return super.process(actions, c);
  }
}

