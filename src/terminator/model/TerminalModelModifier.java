package terminator.model;

import java.awt.Dimension;

public interface TerminalModelModifier {
        void reset();
        void notifyListeners();
        public void setSize(Dimension size);
        public void setStyle(Style style);
	public Style getStyle();
	public void insertLines(int count);
	public void setInsertMode(boolean insertMode);
	public void addText(String text);
	public void setCursorVisible(boolean cursorVisible);
	public void deleteCharacters(int count);
        public void clearToBeginningOfLine();
        public void clearToEndOfLine();
        public void clearToEndOfScreen();
	public void positionCursor(int row, int column);
	public void moveCursorHorizontally(int delta);
	public void moveCursorVertically(int delta);
        public void horizontalTabulation();
        public void lineFeed();
	public void reverseLineFeed();
        public void carriageReturn();
	public void setScrollingRegion(int top, int bottom);
	public void deleteLines(int count);
}
