package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class ClearToEndOfScreen implements TerminalAction {
        public void perform(TerminalModelModifier model) {
                model.clearToEndOfScreen();
        }
        
        public String toString() {
                return "Clear to end of screen";
        }
}
