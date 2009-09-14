package terminator.model;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import e.util.*;
import terminator.terminal.*;
import terminator.view.*;

public class TerminalModel {
	private TerminalView view;
	private int width;
	private int height;
	private ArrayList<TextLine> textLines = new ArrayList<TextLine>();
	private short currentStyle = StyledText.getDefaultStyle();
	private int firstScrollLineIndex;
	private int lastScrollLineIndex;
	private Location cursorPosition;
	private int lastValidStartIndex = 0;
	private boolean insertMode = false;
	private int maxLineWidth = width;
	
	// Used for reducing the number of lines changed events sent up to the view.
	private int firstLineChanged;
	
	public TerminalModel(TerminalView view, int width, int height) {
		this.view = view;
		setSize(width, height);
		cursorPosition = view.getCursorPosition();
	}
	
	public void updateMaxLineWidth(int aLineWidth) {
		maxLineWidth = Math.max(getMaxLineWidth(), aLineWidth);
	}
	
	public int getMaxLineWidth() {
		return Math.max(maxLineWidth, width);
	}
	
	public void checkInvariant() {
		int highestStartLineIndex = -1;
		for (int lineNumber = 0; lineNumber <= lastValidStartIndex; ++ lineNumber) {
			int thisStartLineIndex = textLines.get(lineNumber).getLineStartIndex();
			if (thisStartLineIndex <= highestStartLineIndex) {
				throw new RuntimeException("the lineStartIndex must increase monotonically as the line number increases");
			}
			highestStartLineIndex = thisStartLineIndex;
		}
	}
	
	public void sizeChanged(Dimension sizeInChars) {
		setSize(sizeInChars.width, sizeInChars.height);
		cursorPosition = getLocationWithinBounds(cursorPosition);
	}
	
	private Location getLocationWithinBounds(Location location) {
		if (location == null) {
			return location;
		}
		int lineIndex = Math.min(location.getLineIndex(), textLines.size() - 1);
		int charOffset = Math.min(location.getCharOffset(), width - 1);
		return new Location(lineIndex, charOffset);
	}
	
	private int getNextTabPosition(int charOffset) {
		// No special tab to our right; return the default 8-separated tab stop.
		return (charOffset + 8) & ~7;
	}
	
	/** Returns the length of the indexed line including the terminating NL. */
	public int getLineLength(int lineIndex) {
		return getTextLine(lineIndex).length() + 1;
	}
	
	/** Returns the start character index of the indexed line. */
	public int getStartIndex(int lineIndex) {
		ensureValidStartIndex(lineIndex);
		return getTextLine(lineIndex).getLineStartIndex();
	}
	
	/**
	 * Returns a Location describing the line and offset at which the given char index exists.
	 * If the index is actually larger than the screen area, returns a 'fake' location to the right
	 * of the end of the last line.
	 */
	public Location getLocationFromCharIndex(int charIndex) {
		int lowLine = 0;
		int highLine = textLines.size();
		
		while (highLine - lowLine > 1) {
			int midLine = (lowLine + highLine) / 2;
			int mid = getStartIndex(midLine);
			if (mid <= charIndex) {
				lowLine = midLine;
			} else {
				highLine = midLine;
			}
		}
		return new Location(lowLine, charIndex - getStartIndex(lowLine));
	}
	
	/** Returns the char index equivalent to the given Location. */
	public int getCharIndexFromLocation(Location location) {
		return getStartIndex(location.getLineIndex()) + location.getCharOffset();
	}
	
	/** Returns the count of all characters in the buffer, including NLs. */
	public int length() {
		int lastIndex = textLines.size() - 1;
		return getStartIndex(lastIndex) + getLineLength(lastIndex);
	}
	
	private void lineIsDirty(int dirtyLineIndex) {
		lastValidStartIndex = Math.min(lastValidStartIndex, dirtyLineIndex + 1);
	}
	
	private void ensureValidStartIndex(int lineIndex) {
		if (lineIndex > lastValidStartIndex) {
			for (int i = lastValidStartIndex; i < lineIndex; i++) {
				TextLine line = getTextLine(i);
				getTextLine(i + 1).setLineStartIndex(line.getLineStartIndex() + line.lengthIncludingNewline());
			}
			lastValidStartIndex = lineIndex;
		}
	}
	
	public int getLineCount() {
		return textLines.size();
	}
	
	public void linesChangedFrom(int firstLineChanged) {
		this.firstLineChanged = Math.min(this.firstLineChanged, firstLineChanged);
	}
	
	public Dimension getCurrentSizeInChars() {
		return new Dimension(getMaxLineWidth(), getLineCount());
	}
	
	public Location getCursorPosition() {
		return cursorPosition;
	}
	
	public void processActions(TerminalAction[] actions) {
		firstLineChanged = Integer.MAX_VALUE;
		boolean wereAtBottom = view.isAtBottom();
		boolean needsScroll = false;
		Dimension initialSize = getCurrentSizeInChars();
		for (TerminalAction action : actions) {
			action.perform(this);
		}
		if (firstLineChanged != Integer.MAX_VALUE) {
			needsScroll = true;
			view.linesChangedFrom(firstLineChanged);
		}
		Dimension finalSize = getCurrentSizeInChars();
		if (initialSize.equals(finalSize) == false) {
			view.sizeChanged(initialSize, finalSize);
		}
		if (needsScroll) {
			view.scrollOnTtyOutput(wereAtBottom);
		}
		view.setCursorPosition(cursorPosition);
	}
	
	public void setStyle(short style) {
		this.currentStyle = style;
	}
	
	public short getStyle() {
		return currentStyle;
	}
	
	public void moveToLine(int index) {
		if (index > getFirstDisplayLine() + lastScrollLineIndex) {
			insertLine(index);
		} else {
			cursorPosition = new Location(index, cursorPosition.getCharOffset());
		}
	}
	
	public void insertLine(int index) {
		insertLine(index, new TextLine());
	}
	
	public void insertLine(int index, TextLine lineToInsert) {
		// Use a private copy of the first display line throughout this method to avoid mutation
		// caused by textLines.add()/textLines.remove().
		final int firstDisplayLine = getFirstDisplayLine();
		lineIsDirty(firstDisplayLine);
		if (index > firstDisplayLine + lastScrollLineIndex) {
			for (int i = firstDisplayLine + lastScrollLineIndex + 1; i <= index; i++) {
				textLines.add(i, lineToInsert);
			}
			if (firstScrollLineIndex > 0) {
				// If the program has defined scroll bounds, newline-adding actually chucks away
				// the first scroll line, rather than just scrolling everything upwards like we normally
				// do.  This makes vim work better.  Also, if we're using the alternate buffer, we
				// don't add anything going off the top into the history.
				int removeIndex = firstDisplayLine + firstScrollLineIndex;
				textLines.remove(removeIndex);
				linesChangedFrom(removeIndex);
				view.repaint();
			} else {
				cursorPosition = new Location(index, cursorPosition.getCharOffset());
			}
		} else {
			textLines.remove(firstDisplayLine + lastScrollLineIndex);
			textLines.add(index, lineToInsert);
			linesChangedFrom(index);
			cursorPosition = new Location(index, cursorPosition.getCharOffset());
		}
		checkInvariant();
	}
	
	public int getFirstDisplayLine() {
		return textLines.size() - height;
	}
	
	public int getWidth() {
		return width;
	}
	
	public TextLine getTextLine(int index) {
		if (index >= textLines.size()) {
			Log.warn("TextLine requested for index " + index + ", size of buffer is " + textLines.size() + ".", new Exception("stack trace"));
			return new TextLine();
		}
		return textLines.get(index);
	}
	
	public void setSize(int width, int height) {
		this.width = width;
		lineIsDirty(0);
		if (this.height > height && textLines.size() >= this.height) {
			for (int i = 0; i < (this.height - height); i++) {
				int lineToRemove = textLines.size() - 1;
				if (getTextLine(lineToRemove).length() == 0 && cursorPosition.getLineIndex() != lineToRemove) {
					textLines.remove(lineToRemove);
				}
			}
		} else if (this.height < height) {
			for (int i = 0; i < (height - this.height); i++) {
				if (getFirstDisplayLine() <= 0) {
					textLines.add(new TextLine());
				}
			}
		}
		this.height = height;
		firstScrollLineIndex = 0;
		lastScrollLineIndex = height - 1;
		while (getFirstDisplayLine() < 0) {
			textLines.add(new TextLine());
		}
		checkInvariant();
	}
	
	public void setInsertMode(boolean insertMode) {
		this.insertMode = insertMode;
	}
	
	/**
	 * Process the characters in the given line. The string is composed of
	 * normal printable characters, escape sequences having been extracted
	 * elsewhere.
	 */
	public void processLine(String untranslatedLine) {
		String line = view.getTerminalControl().translate(untranslatedLine);
		TextLine textLine = getTextLine(cursorPosition.getLineIndex());
		if (insertMode) {
			//Log.warn("Inserting text \"" + line + "\" at " + cursorPosition + ".");
			textLine.insertTextAt(cursorPosition.getCharOffset(), line, currentStyle);
		} else {
			//Log.warn("Writing text \"" + line + "\" at " + cursorPosition + ".");
			textLine.writeTextAt(cursorPosition.getCharOffset(), line, currentStyle);
		}
		textAdded(line.length());
	}
	
	private void textAdded(int length) {
		TextLine textLine = getTextLine(cursorPosition.getLineIndex());
		updateMaxLineWidth(textLine.length());
		lineIsDirty(cursorPosition.getLineIndex() + 1);  // cursorPosition's line still has a valid *start* index.
		linesChangedFrom(cursorPosition.getLineIndex());
		moveCursorHorizontally(length);
	}
	
	public void processSpecialCharacter(char ch) {
		switch (ch) {
		case Ascii.CR:
			cursorPosition = new Location(cursorPosition.getLineIndex(), 0);
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
		TextLine textLine = getTextLine(cursorPosition.getLineIndex());
		int startOffset = cursorPosition.getCharOffset();
		int tabLength = nextTabLocation - startOffset;
		// We want to insert our special tabbing characters (see getTabString) when inserting a tab or outputting one at the end of a line, so that text copied from the output of (say) cat(1) will be pasted with tabs preserved.
		boolean endOfLine = (startOffset == textLine.length());
		if (insertMode || endOfLine) {
			textLine.insertTabAt(startOffset, tabLength, currentStyle);
		} else {
			// Emacs, source of all bloat, uses \t\b\t sequences around tab stops (in lines with no \t characters) if you hold down right arrow. The call to textAdded below moves the cursor, which is all we're supposed to do.
		}
		textAdded(tabLength);
	}
	
	/** Sets whether the cursor should be visible. */
	public void setCursorVisible(boolean isDisplayed) {
		view.setCursorVisible(isDisplayed);
	}
	
	/** Inserts lines at the current cursor position. */
	public void insertLines(int count) {
		for (int i = 0; i < count; i++) {
			insertLine(cursorPosition.getLineIndex());
		}
	}
	
	public void deleteCharacters(int count) {
		TextLine line = getTextLine(cursorPosition.getLineIndex());
		int start = cursorPosition.getCharOffset();
		int end = start + count;
		line.killText(start, end);
		lineIsDirty(cursorPosition.getLineIndex() + 1);  // cursorPosition.y's line still has a valid *start* index.
		linesChangedFrom(cursorPosition.getLineIndex());
	}
	
	public void killHorizontally(boolean fromStart, boolean toEnd) {
		TextLine line = getTextLine(cursorPosition.getLineIndex());
		int oldLineLength = line.length();
		int start = fromStart ? 0 : cursorPosition.getCharOffset();
		int end = toEnd ? oldLineLength : cursorPosition.getCharOffset();
		line.killText(start, end);
		lineIsDirty(cursorPosition.getLineIndex() + 1);  // cursorPosition.y's line still has a valid *start* index.
		linesChangedFrom(cursorPosition.getLineIndex());
	}
	
	/** Erases from either the top or the cursor, to either the bottom or the cursor. */
	public void eraseInPage(boolean fromTop, boolean toBottom) {
		// Should produce "hi\nwo":
		// echo $'\n\n\nworld\x1b[A\rhi\x1b[B\x1b[J'
		// Should produce "   ld":
		// echo $'\n\n\nworld\x1b[A\rhi\x1b[B\x1b[1J'
		// Should clear the screen:
		// echo $'\n\n\nworld\x1b[A\rhi\x1b[B\x1b[2J'
		int start = fromTop ? getFirstDisplayLine() : cursorPosition.getLineIndex();
		int startClearing = fromTop ? start : start + 1;
		int endClearing = toBottom ? getLineCount() : cursorPosition.getLineIndex();
		for (int i = startClearing; i < endClearing; i++) {
			getTextLine(i).clear();
		}
		TextLine line = getTextLine(cursorPosition.getLineIndex());
		int oldLineLength = line.length();
		if (fromTop) {
			// The current position is always erased, hence the + 1.
			// Is overwriting with spaces in the currentStyle correct?
			line.writeTextAt(0, StringUtilities.nCopies(cursorPosition.getCharOffset() + 1, ' '), currentStyle);
		}
		if (toBottom) {
			line.killText(cursorPosition.getCharOffset(), oldLineLength);
		}
		lineIsDirty(start + 1);
		linesChangedFrom(start);
	}
	
	/**
	 * Sets the position of the cursor to the given x and y coordinates, counted from 1,1 at the top-left corner.
	 * If either x or y is -1, that coordinate is left unchanged.
	 */
	public void setCursorPosition(int x, int y) {
		// Although the cursor positions are supposed to be measured
		// from (1,1), there's nothing to stop a badly-behaved program
		// from sending (0,0). ASUS routers do this (they're rubbish).
		
		int charOffset = cursorPosition.getCharOffset();
		if (x != -1) {
			// Translate from 1-based coordinates to 0-based.
			charOffset = Math.max(0, x - 1);
			charOffset = Math.min(charOffset, width - 1);
		}
		
		int lineIndex = cursorPosition.getLineIndex();
		if (y != -1) {
			// Translate from 1-based coordinates to 0-based.
			int lineOffsetFromStartOfDisplay = Math.max(0, y - 1);
			lineOffsetFromStartOfDisplay = Math.min(lineOffsetFromStartOfDisplay, height - 1);
			// Although the escape sequence was in terms of a line on the display, we need to take the lines above the display into account.
			lineIndex = getFirstDisplayLine() + lineOffsetFromStartOfDisplay;
		}
		
		cursorPosition = new Location(lineIndex, charOffset);
	}
	
	/** Moves the cursor horizontally by the number of characters in xDiff, negative for left, positive for right. */
	public void moveCursorHorizontally(int xDiff) {
		int charOffset = cursorPosition.getCharOffset() + xDiff;
		int lineIndex = cursorPosition.getLineIndex();
		// Test cases:
		// /bin/echo -e 'hello\n\bhello'
		// /bin/echo -e 'hello\n\033[1Dhello'
		if (charOffset < 0) {
			charOffset = 0;
		}
		// Constraining charOffset here stops line editing working properly on Titan serial consoles.
		//charOffset = Math.min(charOffset, width - 1);
		cursorPosition = new Location(lineIndex, charOffset);
	}
	
	/** Moves the cursor vertically by the number of characters in yDiff, negative for up, positive for down. */
	public void moveCursorVertically(int yDiff) {
		int y = cursorPosition.getLineIndex() + yDiff;
		y = Math.max(getFirstDisplayLine(), y);
		y = Math.min(y, textLines.size() - 1);
		cursorPosition = new Location(y, cursorPosition.getCharOffset());
	}
	
	/** Sets the first and last lines to scroll.  If both are -1, make the entire screen scroll. */
	public void setScrollingRegion(int firstLine, int lastLine) {
		firstScrollLineIndex = ((firstLine == -1) ? 1 : firstLine) - 1;
		lastScrollLineIndex = ((lastLine == -1) ? height : lastLine) - 1;
	}
	
	/** Scrolls the display up by one line. */
	public void scrollDisplayUp() {
		int addIndex = getFirstDisplayLine() + firstScrollLineIndex;
		int removeIndex = getFirstDisplayLine() + lastScrollLineIndex + 1;
		textLines.add(addIndex, new TextLine());
		textLines.remove(removeIndex);
		lineIsDirty(addIndex);
		linesChangedFrom(addIndex);
		view.repaint();
		checkInvariant();
	}
	
	/** Delete one line, moving everything below up and inserting a blank line at the bottom. */
	public void deleteLine() {
		int removeIndex = cursorPosition.getLineIndex();
		int addIndex = getFirstDisplayLine() + lastScrollLineIndex + 1;
		textLines.add(addIndex, new TextLine());
		textLines.remove(removeIndex);
		lineIsDirty(removeIndex);
		linesChangedFrom(removeIndex);
		view.repaint();
		checkInvariant();
	}
}
