package terminator.terminal.states;

import terminator.terminal.*;

public class CSIIgnoreState extends State {
  private static CSIIgnoreState instance = new CSIIgnoreState();

  public static State enter() {
    return instance;
  }

  public State process(ActionQueue actions, char c) {
    if (0x40 <= c && c <= 0x7e)
      return GroundState.enter();
    return super.process(actions, c);
  }
}
