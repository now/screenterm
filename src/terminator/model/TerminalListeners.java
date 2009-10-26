package terminator.model;

import java.util.*;

class TerminalListeners implements TerminalListener {
        private List<TerminalListener> listeners = new ArrayList<TerminalListener>();

        public void add(TerminalListener l) {
                listeners.add(l);
        }

        public void contentsChanged(int fromLine) {
                for (TerminalListener l : listeners)
                        l.contentsChanged(fromLine);
        }

        public void cursorPositionChanged(Cursor oldCursor, Cursor newCursor) {
                for (TerminalListener l : listeners)
                        l.cursorPositionChanged(oldCursor, newCursor);
        }

        public void cursorVisibilityChanged(boolean isVisible) {
                for (TerminalListener l : listeners)
                        l.cursorVisibilityChanged(isVisible);
        }
}
