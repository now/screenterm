package terminator.model;

public interface TerminalListener {
        public void contentsChanged(int fromLine);
        public void cursorPositionChanged(Location oldPosition, Location newPosition);
        public void cursorVisibilityChanged(boolean isVisible);
}
