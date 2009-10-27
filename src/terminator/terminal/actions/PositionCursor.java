package terminator.terminal.actions;

import terminator.model.*;
import terminator.terminal.*;

public class PositionCursor implements TerminalAction {
        private int row;
        private int column;

        public PositionCursor(int row, int column) {
                this.row = row;
                this.column = column;
        }

        public void perform(TerminalModelModifier model) {
                model.positionCursor(row, column);
        }
        
        public String toString() {
                return "Move cursor to row " + row + ", column " + column;
        }
}
