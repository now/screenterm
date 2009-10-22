package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class HorizontalTabulation implements TerminalAction {
        public void perform(TerminalModelModifier model) {
                model.horizontalTabulation();
        }
        
        public String toString() {
                return "Horizontal tabulation";
        }
}
