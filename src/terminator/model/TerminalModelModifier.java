package terminator.model;

import java.awt.Dimension;

public interface TerminalModelModifier {
        void reset();
        void notifyListeners();
        public void setSize(Dimension size);
        public void setStyle(short style);
	public short getStyle();
	public void moveToLine(int index);
	public void insertLines(int count);
	public void setInsertMode(boolean insertMode);
	public void processLine(String line);
	public void processSpecialCharacter(char c);
	public void setCursorVisible(boolean cursorVisible);
	public void deleteCharacters(int count);
	public void killHorizontally(boolean fromStart, boolean toEnd);
	public void eraseInPage(boolean fromTop, boolean toBottom);
	public void setCursorPosition(int x, int y);
	public void moveCursorHorizontally(int delta);
	public void moveCursorVertically(int delta);
	public void setScrollingRegion(int firstLine, int lastLine);
	public void scrollDisplayUp();
	public void deleteLine();
}
