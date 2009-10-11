package terminator.model;

import java.awt.Dimension;
import java.util.*;
import e.util.*;
import terminator.terminal.*;

public class TerminalModel {
        private class TerminalListeners implements TerminalListener {
                private List<TerminalListener> listeners = new ArrayList<TerminalListener>();

                public void add(TerminalListener l) {
                        listeners.add(l);
                }

                public void contentsChanged(int fromLine) {
                        for (TerminalListener l : listeners)
                                l.contentsChanged(fromLine);
                }

                public void cursorPositionChanged(Cursor oldPosition, Cursor newPosition) {
                        for (TerminalListener l : listeners)
                                l.cursorPositionChanged(oldPosition, newPosition);
                }

                public void cursorVisibilityChanged(boolean isVisible) {
                        for (TerminalListener l : listeners)
                                l.cursorVisibilityChanged(isVisible);
                }
        }

        private TerminalListeners listeners = new TerminalListeners();
        private TextLines textLines = new TextLines(new Dimension(0, 0));
        private short currentStyle = StyledText.getDefaultStyle();
        private int firstScrollLineIndex;
        private int lastScrollLineIndex;
        private Cursor cursorPosition = new Cursor(new Dimension(0, 0), 0, 0);
        private boolean cursorVisible = true;
        private boolean insertMode = false;

	public void addListener(TerminalListener l) {
		listeners.add(l);
	}

	public int getLineCount() {
                return textLines.count();
	}

	public TextLine getTextLine(int index) {
                return textLines.get(index);
	}

	public Cursor getCursorPosition() {
		return cursorPosition;
	}

        public boolean getCursorVisible() {
                return cursorVisible;
        }

	public void processActions(TerminalAction[] actions) {
                modifier.reset();
		for (TerminalAction action : actions)
			action.perform(modifier);
                modifier.notifyListeners();
	}

        private TerminalModelModifier modifier = new TerminalModelModifier() {
                private int firstLineChanged;
                private Cursor oldCursorPosition;

                public void reset() {
                        firstLineChanged = Integer.MAX_VALUE;
                        oldCursorPosition = cursorPosition;
                }

                public void notifyListeners() {
                        if (!oldCursorPosition.equals(cursorPosition))
                                listeners.cursorPositionChanged(oldCursorPosition, cursorPosition);
                        if (firstLineChanged != Integer.MAX_VALUE)
                                listeners.contentsChanged(firstLineChanged);
                }

                private void linesChangedFrom(int line) {
                        firstLineChanged = Math.min(firstLineChanged, line);
                }

                private void linesChangedFromCursorPosition() {
                        linesChangedFrom(cursorPosition.getLineIndex());
                }

                public void setSize(Dimension size) {
                        textLines.setSize(size);
                        firstScrollLineIndex = 0;
                        lastScrollLineIndex = getLineCount() - 1;
                        cursorPosition = new Cursor(size,
                                                      cursorPosition.getLineIndex(),
                                                      cursorPosition.getCharOffset());
                }

                public void setStyle(short style) {
                        currentStyle = style;
                }

                public short getStyle() {
                        return currentStyle;
                }

                public void moveToLine(int index) {
                        // NOTE: We only really allow index to be lastScrollLineIndex + 1
                        if (index > lastScrollLineIndex)
                                insertLines(index, 1);
                        else
                                cursorPosition = cursorPosition.moveToLine(index);
                }

                public void insertLines(int count) {
                        insertLines(cursorPosition.getLineIndex(), count);
                }

                private void insertLines(int at, int count) {
                        int above = textLines.insertLines(at,
                                                          count,
                                                          firstScrollLineIndex,
                                                          lastScrollLineIndex);
                        linesChangedFrom(above == count ? cursorPosition.getLineIndex() : firstScrollLineIndex);
                        if (above > 0)
                                moveCursorVertically(above);
                }

                public void setInsertMode(boolean newInsertMode) {
                        insertMode = newInsertMode;
                }

                public void processLine(String line) {
                        TextLine textLine = getCursorTextLine();
                        if (insertMode) {
                                //Log.warn("Inserting text \"" + line + "\" at " + cursorPosition + ".");
                                textLine.insertTextAt(cursorPosition.getCharOffset(), line, currentStyle);
                        } else {
                                //Log.warn("Writing text \"" + line + "\" at " + cursorPosition + ".");
                                textLine.writeTextAt(cursorPosition.getCharOffset(), line, currentStyle);
                        }
                        textAdded(line.length());
                }

                private TextLine getCursorTextLine() {
                        return textLines.get(cursorPosition.getLineIndex());
                }

                private void textAdded(int length) {
                        moveCursorHorizontally(length);
                        linesChangedFromCursorPosition();
                }

                public void processSpecialCharacter(char ch) {
                        switch (ch) {
                        case Ascii.CR:
                                cursorPosition = cursorPosition.moveToChar(0);
                                return;
                        case Ascii.LF:
                                moveToLine(cursorPosition.getLineIndex() + 1);
                                return;
                        case Ascii.VT:
                                moveCursorVertically(1);
                                return;
                        case Ascii.HT:
                                insertTab();
                                return;
                        case Ascii.BS:
                                moveCursorHorizontally(-1);
                                return;
                        default:
                                Log.warn("Unsupported special character: " + ((int) ch));
                        }
                }

                private void insertTab() {
                        int nextTabLocation = getNextTabPosition(cursorPosition.getCharOffset());
                        TextLine textLine = getCursorTextLine();
                        int startOffset = cursorPosition.getCharOffset();
                        int tabLength = nextTabLocation - startOffset;
                        // We want to insert our special tabbing characters
                        // (see getTabString) when inserting a tab or
                        // outputting one at the end of a line, so that text
                        // copied from the output of (say) cat(1) will be
                        // pasted with tabs preserved.
                        boolean endOfLine = (startOffset == textLine.length());
                        if (insertMode || endOfLine) {
                                textLine.insertTabAt(startOffset, tabLength, currentStyle);
                        } else {
                                // Emacs, source of all bloat, uses \t\b\t
                                // sequences around tab stops (in lines with no
                                // \t characters) if you hold down right arrow.
                                // The call to textAdded below moves the
                                // cursor, which is all we're supposed to do.
                        }
                        textAdded(tabLength);
                }

                private int getNextTabPosition(int charOffset) {
                        return (charOffset + 8) & ~7;
                }

                public void setCursorVisible(boolean visible) {
                        cursorVisible = visible;
                        listeners.cursorVisibilityChanged(visible);
                }

                public void deleteCharacters(int count) {
                        getCursorTextLine().killText(cursorPosition.getCharOffset(),
                                                     cursorPosition.getCharOffset() + count);
                        linesChangedFromCursorPosition();
                }

                public void killHorizontally(boolean fromStart, boolean toEnd) {
                        TextLine line = getCursorTextLine();
                        line.killText(fromStart ? 0 : cursorPosition.getCharOffset(),
                                      toEnd ? line.length() : cursorPosition.getCharOffset());
                        linesChangedFromCursorPosition();
                }

                public void eraseInPage(boolean fromTop, boolean toBottom) {
                        // Should produce "hi\nwo":
                        // echo $'\n\n\nworld\x1b[A\rhi\x1b[B\x1b[J'
                        // Should produce "   ld":
                        // echo $'\n\n\nworld\x1b[A\rhi\x1b[B\x1b[1J'
                        // Should clear the screen:
                        // echo $'\n\n\nworld\x1b[A\rhi\x1b[B\x1b[2J'
                        int start = fromTop ? 0 : cursorPosition.getLineIndex();
                        int startClearing = fromTop ? start : start + 1;
                        int endClearing = toBottom ? getLineCount() : cursorPosition.getLineIndex();
                        for (int i = startClearing; i < endClearing; i++) {
                                getTextLine(i).clear();
                        }
                        TextLine line = getCursorTextLine();
                        int oldLineLength = line.length();
                        if (fromTop) {
                                // The current position is always erased, hence the + 1.
                                // Is overwriting with spaces in the currentStyle correct?
                                line.writeTextAt(0, StringUtilities.nCopies(cursorPosition.getCharOffset() + 1, ' '), currentStyle);
                        }
                        if (toBottom) {
                                line.killText(cursorPosition.getCharOffset(), oldLineLength);
                        }
                        linesChangedFrom(start);
                }

               public void setCursorPosition(int x, int y) {
                        if (x != -1)
                                cursorPosition = cursorPosition.moveToChar(x - 1);
                        if (y != -1)
                                cursorPosition = cursorPosition.moveToLine(y - 1);
                }

                public void moveCursorHorizontally(int delta) {
                        cursorPosition = cursorPosition.adjustCharOffset(delta);
                }

                public void moveCursorVertically(int delta) {
                        cursorPosition = cursorPosition.adjustLineIndex(delta);
                }

                public void setScrollingRegion(int firstLine, int lastLine) {
                        firstScrollLineIndex = ((firstLine == -1) ? 1 : firstLine) - 1;
                        lastScrollLineIndex = ((lastLine == -1) ? getLineCount() : lastLine) - 1;
                }

                public void scrollDisplayUp() {
                        modifyOneLine(firstScrollLineIndex, firstScrollLineIndex, lastScrollLineIndex + 1);
                }

                private void modifyOneLine(int index, int top, int bottom) {
                        textLines.insertLines(index, 1, 0, bottom);
                        linesChangedFrom(top);
                }

                public void deleteLine() {
                        modifyOneLine(lastScrollLineIndex + 1, cursorPosition.getLineIndex(), lastScrollLineIndex);
                }
        };
}
