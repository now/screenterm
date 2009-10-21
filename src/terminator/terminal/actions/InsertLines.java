package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class InsertLines implements TerminalAction {
        private int count;

        public InsertLines(int count) {
                this.count = count;
        }

        public void perform(TerminalModelModifier model) {
                model.insertLines(count);
        }
        
        public String toString() {
                return "Insert " + count + " lines";
        }
}
