package terminator.terminal.states;

import terminator.model.*;
import terminator.terminal.*;
import terminator.terminal.actions.*;
import terminator.terminal.charactersets.*;
import terminator.util.*;

public abstract class State {
  public State process(ActionQueue actions, char c) {
    switch (c) {
    case Ascii.BS: return add(actions, new MoveCursorLeft(1));
    case Ascii.HT: return add(actions, new HorizontalTabulation());
    case Ascii.LF: return add(actions, new LineFeed());
    case Ascii.VT: return add(actions, new MoveCursorDown(1));
    case Ascii.CR: return add(actions, new CarriageReturn());
    case Ascii.SO: return setCharacterSet(new GraphicalCharacterSet());
    case Ascii.SI: return setCharacterSet(new NormalCharacterSet());
    case Ascii.CAN:
    case Ascii.SUB: return GroundState.enter();
    case Ascii.ESC: return EscapeState.enter();
    default: return this;
    }
  }

  private State add(ActionQueue actions, TerminalAction action) {
    actions.add(action);
    return this;
  }

  private State setCharacterSet(CharacterSet characterSet) {
    GroundState.setCharacterSet(characterSet);
    return this;
  }
}
