package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class LineFeed implements TerminalAction {
        public void perform(TerminalModelModifier model) {
                model.lineFeed();
        }
        
        public String toString() {
                return "Line feed";
        }
}
