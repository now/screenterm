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
        private Cursor cursor = Cursor.ORIGO;

	public void addListener(TerminalListener l) {
		listeners.add(l);
	}

	public int getLineCount() {
                return textLines.count();
	}

	public TextLine getTextLine(int index) {
                return textLines.get(index);
	}

	public Cursor getCursor() {
		return cursor;
	}

        public List<TextLine> region(int from, int to) {
                return textLines.region(from, Math.min(to, textLines.count()));
        }

	public void processActions(TerminalAction[] actions) {
                modifier.reset();
		for (TerminalAction action : actions)
			action.perform(modifier);
                modifier.notifyListeners();
	}

        private TerminalModelModifier modifier = new TerminalModelModifier() {
                private Style style = Style.DEFAULT;
                private Region scrollingRegion = new Region(0, 0);
                private boolean insertMode = false;
                private int firstLineChanged;
                private Cursor oldCursor;

                public void reset() {
                        firstLineChanged = Integer.MAX_VALUE;
                        oldCursor = cursor;
                }

                public void notifyListeners() {
                        if (!oldCursor.equals(cursor))
                                listeners.cursorPositionChanged(oldCursor, cursor);
                        if (firstLineChanged != Integer.MAX_VALUE)
                                listeners.contentsChanged(firstLineChanged);
                }

                private void linesChangedFrom(int line) {
                        firstLineChanged = Math.min(firstLineChanged, line);
                }

                private void linesChangedFromCursor() {
                        linesChangedFrom(cursor.row());
                }

                public void setSize(Dimension size) {
                        textLines.setSize(size);
                        scrollingRegion.set(0, getLineCount() - 1);
                        cursor = cursor.constrain(size);
                }

                public void setStyle(Style style) {
                        this.style = style;
                }

                public Style getStyle() {
                        return style;
                }

                public void insertLines(int count) {
                        insertLines(cursor.row(), count);
                }

                private void insertLines(int at, int count) {
                        int above = textLines.insertLines(at,
                                                          count,
                                                          scrollingRegion.top(),
                                                          scrollingRegion.bottom());
                        linesChangedFrom(above == count ? cursor.row() : scrollingRegion.top());
                }

                public void setInsertMode(boolean newInsertMode) {
                        insertMode = newInsertMode;
                }

                public void addText(String text) {
                        TextLine textLine = getCursorTextLine();
                        if (insertMode)
                                textLine.insertTextAt(cursor.column(), text, style);
                        else
                                textLine.writeTextAt(cursor.column(), text, style);
                        textAdded(text.length());
                }

                private TextLine getCursorTextLine() {
                        return textLines.get(cursor.row());
                }

                private void textAdded(int length) {
                        moveCursorHorizontally(length);
                        linesChangedFromCursor();
                }

                public void horizontalTabulation() {
                        TextLine textLine = getCursorTextLine();
                        int length = nextTabColumn(cursor.column()) - cursor.column();
                        if (insertMode || cursor.column() == textLine.length())
                                textLine.insertTabAt(cursor.column(), length, style);
                        textAdded(length);
                }

                private int nextTabColumn(int column) {
                        return (column + 8) & ~7;
                }

                public void lineFeed() {
                        int row = cursor.row() + 1;
                        if (row > scrollingRegion.bottom())
                                insertLines(row, 1);
                        else
                                cursor = cursor.moveToRow(row);
                }

                public void carriageReturn() {
                        cursor = cursor.moveToColumn(0);
                }

                public void setCursorVisible(boolean visible) {
                        if (cursor.isVisible() == visible)
                                return;
                        cursor = cursor.setVisible(visible);
                        listeners.cursorVisibilityChanged(visible);
                }

                public void deleteCharacters(int count) {
                        getCursorTextLine().clear(cursor.column(), count);
                        linesChangedFromCursor();
                }

                public void clearToEndOfLine() {
                        getCursorTextLine().clearFrom(cursor.column());
                        linesChangedFromCursor();
                }

                public void clearToBeginningOfLine() {
                        getCursorTextLine().clear(0, cursor.column());
                        linesChangedFromCursor();
                }

                public void clearToEndOfScreen() {
                        clearToEndOfLine();
                        textLines.clearFrom(cursor.row() + 1);
                }

                public void setCursorPosition(int row, int column) {
                        cursor = cursor.moveToRow(row).moveToColumn(column);
                }

                public void moveCursorHorizontally(int delta) {
                        cursor = cursor.adjustColumn(delta);
                }

                public void moveCursorVertically(int delta) {
                        cursor = cursor.adjustRow(delta);
                }

                public void setScrollingRegion(int top, int bottom) {
                        if (bottom > getLineCount() - 1) {
                                Log.warn("Tried to set scrolling region bottom beyond last line" +
                                         " (" + bottom + " > " + (getLineCount() - 1) + ")");
                                return;
                        }
                        if (top < 0)
                                top = 0;
                        if (bottom < 0)
                                bottom = getLineCount() - 1;
                        scrollingRegion.set(top, bottom);
                }

                public void scrollDisplayUp() {
                        insertLines(scrollingRegion.top(), 1);
                        linesChangedFrom(scrollingRegion.top());
                }

                public void deleteLines(int count) {
                        textLines.insertLines(scrollingRegion.bottom() + 1,
                                              count,
                                              cursor.row(),
                                              scrollingRegion.bottom());
                        linesChangedFromCursor();
                }
        };
}
