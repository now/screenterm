package terminator.terminal.states;

import e.util.*;

import terminator.terminal.*;
import terminator.terminal.actions.*;
import terminator.terminal.charactersets.*;

public abstract class State {
        public State process(ActionQueue actions, char c) {
                switch (c) {
                case Ascii.BS:
                        actions.add(new MoveCursorLeft(1));
                        return this;
                case Ascii.HT:
                        actions.add(new HorizontalTabulation());
                        return this;
                case Ascii.LF:
                        actions.add(new LineFeed());
                        return this;
                case Ascii.VT:
                        actions.add(new MoveCursorDown(1));
                        return this;
                case Ascii.CR:
                        actions.add(new CarriageReturn());
                        return this;
                case Ascii.SO:
                        GroundState.setCharacterSet(new GraphicalCharacterSet());
                        return this;
                case Ascii.SI:
                        GroundState.setCharacterSet(new NormalCharacterSet());
                        return this;
                case Ascii.CAN:
                case Ascii.SUB:
                        return GroundState.enter();
                case Ascii.ESC:
                        return EscapeState.enter();
                default:
                        return this;
                }
        }
}
