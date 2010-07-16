package terminator.model;

public interface TerminalListener {
  public void contentsChanged(int fromLine);
  public void cursorPositionChanged(Cursor oldPosition, Cursor newPosition);
  public void cursorVisibilityChanged(boolean isVisible);
}
