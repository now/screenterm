package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class ScrollDisplayUp implements TerminalAction {
        public void perform(TerminalModelModifier model) {
                model.scrollDisplayUp();
        }
        
        public String toString() {
                return "Scroll display up";
        }
}
