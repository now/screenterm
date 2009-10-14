package terminator.model;

import java.awt.Dimension;

public interface TerminalModelModifier {
        void reset();
        void notifyListeners();
        public void setSize(Dimension size);
        public void setStyle(short style);
	public short getStyle();
	public void insertLines(int count);
	public void setInsertMode(boolean insertMode);
	public void processLine(String line);
	public void processSpecialCharacter(char c);
	public void setCursorVisible(boolean cursorVisible);
	public void deleteCharacters(int count);
        public void clearToBeginningOfLine();
        public void clearToEndOfLine();
        public void clearToEndOfScreen();
	public void setCursorPosition(int row, int column);
	public void moveCursorHorizontally(int delta);
	public void moveCursorVertically(int delta);
	public void setScrollingRegion(int top, int bottom);
	public void scrollDisplayUp();
	public void deleteLines(int count);
}
