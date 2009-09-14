package terminator.model;

import java.util.*;

/**
 * Ties together the String containing the characters on a particular line, and the styles to be applied to each character.
 * TextLines are mutable, though it's not possible to change style information without rewriting the corresponding characters (because that's not how terminals work).
 */
public class TextLine {
	// The text we store internally contains information about tabs.
	// When text is passed back out to the outside world, we either convert the tab information to spaces (for the display), or to tab characters (for the clipboard).
	// Internally, a tab is marked as beginning with TAB_START.
	// Each following display position (assuming *all* characters are the same width) covered by the tab is denoted by TAB_CONTINUE.
	// We have to internally store all this tab position and length information because tab positions can change in the outside world at any time, but each TextLine must retain its integrity once the tabs have been inserted into it.
	private static final char TAB_START = '\t';
	private static final char TAB_CONTINUE = '\r';
	
	// The characters on this line.
	// An immutable String may seem like an odd choice, but we've tried StringBuilder too.
	// In terms of space, StringBuilder helps a little, saving on useless String fields (such as the cached hashCode), but we pay extra for each blank line (where the cost is a whole new StringBuilder rather than just sharing the JVM's single empty-string instance), and we pay for unused space in the underlying char[]s.
	// In terms of time, StringBuilder hurts a little, because we need to convert to a String for our callers (especially rendering), and young lines don't change much and old lines never change.
	// In terms of code, there's nothing in it; the StringBuilder delete and insert methods are arguably more readable, but that only affects a handful of lines.
	// All in all, then, String is actually the best choice in our current environment.
	// (If we switched rendering over to AttributedCharacterIterator or something else that didn't require a String, that might change the balance.)
	private String text;
	
	// The styles to be applied to the characters on this line.
	// styles == null => all characters use the default style.
	// Otherwise, styles.length == text.length(), and the style information for text.charAt(i) is styles[i].
	private short[] styles;
	
	public TextLine() {
		clear();
	}
	
	public short getStyleAt(int index) {
		return (styles == null) ? StyledText.getDefaultStyle() : styles[index];
	}
	
	public List<StyledText> getStyledTextSegments(int widthHintInChars) {
		final int textLength = text.length();
		if (textLength == 0) {
			return Collections.emptyList();
		}
		String string = getString();
		ArrayList<StyledText> result = new ArrayList<StyledText>();
		int startIndex = 0;
		short startStyle = getStyleAt(0);
		boolean haveReasonToChop = (styles != null || string.length() > widthHintInChars);
		if (haveReasonToChop) {
			// If the line is very long, it helps the rendering code's manual clipping if we split it into more segments than necessary.
			// Note: mod of a non-constant is too expensive, so we pay for a single decrement instead.
			int charsLeftBeforeSplit = widthHintInChars;
			for (int i = 1; i < textLength; ++i) {
				if ((styles != null && styles[i] != startStyle) || (--charsLeftBeforeSplit == 0)) {
					result.add(new StyledText(string.substring(startIndex, i), startStyle));
					startIndex = i;
					if (styles != null) {
						startStyle = styles[i];
					}
					if (charsLeftBeforeSplit == 0) {
						charsLeftBeforeSplit = widthHintInChars;
					}
				}
			}
		}
		result.add(new StyledText(string.substring(startIndex, textLength), startStyle));
		return result;
	}
	
	/**
	 * Returns the text of this line with spaces instead of tabs (or, indeed, instead of the special representation we use internally).
	 * 
	 * This isn't called toString because you need to come here and think about whether you want this method or getTabbedString instead.
	 */
	public String getString() {
		return text.replace(TAB_START, ' ').replace(TAB_CONTINUE, ' ');
	}
	
	public int length() {
		return text.length();
	}
	
	public void clear() {
		text = "";
		styles = null;
	}
	
	public void killText(int startIndex, int endIndex) {
		if (startIndex >= endIndex || startIndex >= text.length()) {
			return;
		}
		endIndex = Math.min(endIndex, text.length());
		text = text.substring(0, startIndex) + text.substring(endIndex);
		removeStyleData(startIndex, endIndex);
	}
	
	public void insertTabAt(int offset, int tabLength, short style) {
		insertTextAt(offset, getTabString(tabLength), style);
	}
	
	private static String getTabString(int tabLength) {
		char[] tab = new char[tabLength];
		tab[0] = TAB_START;
		Arrays.fill(tab, 1, tab.length, TAB_CONTINUE);
		return new String(tab);
	}
	
	/** Inserts text at the given position, moving anything already there further to the right. */
	public void insertTextAt(int offset, String newText, short style) {
		ensureOffsetIsOK(offset);
		text = text.substring(0, offset) + newText + text.substring(offset);
		insertStyleData(offset, newText.length(), style);
	}
	
	/** Writes text at the given position, overwriting anything underneath. */
	public void writeTextAt(int offset, String newText, short style) {
		ensureOffsetIsOK(offset);
		if (offset + newText.length() < text.length()) {
			text = text.substring(0, offset) + newText + text.substring(offset + newText.length());
		} else {
			text = text.substring(0, offset) + newText;
		}
		overwriteStyleData(offset, newText.length(), style);
	}
	
	private void ensureOffsetIsOK(int offset) {
		if (offset < 0) {
			throw new IllegalArgumentException("Negative offset " + offset);
		}
		if (offset > text.length()) {
			appendPadding(offset - text.length());
		}
	}
	
	private void appendPadding(int count) {
		char[] pad = new char[count];
		Arrays.fill(pad, ' ');
		int oldTextLength = text.length();
		text += new String(pad);
		insertStyleData(oldTextLength, count, StyledText.getDefaultStyle());
	}
	
	private void overwriteStyleData(int offset, int count, short value) {
		if (styles == null && value == StyledText.getDefaultStyle()) {
			return;
		}
		short[] oldStyleData = maybeResizeStyleData();
		if (oldStyleData != null) {
			System.arraycopy(oldStyleData, 0, styles, 0, oldStyleData.length);
		} else {
			Arrays.fill(styles, 0, offset, StyledText.getDefaultStyle());
		}
		Arrays.fill(styles, offset, offset + count, value);
	}
	
	private void insertStyleData(int offset, int count, short value) {
		if (styles == null && value == StyledText.getDefaultStyle()) {
			return;
		}
		short[] oldStyleData = maybeResizeStyleData();
		if (oldStyleData != null) {
			System.arraycopy(oldStyleData, 0, styles, 0, offset);
			Arrays.fill(styles, offset, offset + count, value);
			System.arraycopy(oldStyleData, offset, styles, offset + count, oldStyleData.length - offset);
		} else {
			Arrays.fill(styles, 0, styles.length, StyledText.getDefaultStyle());
			Arrays.fill(styles, offset, offset + count, value);
		}
	}
	
	private void removeStyleData(int startIndex, int endIndex) {
		if (styles == null) {
			return;
		}
		short[] oldStyleData = maybeResizeStyleData();
		System.arraycopy(oldStyleData, 0, styles, 0, startIndex);
		System.arraycopy(oldStyleData, endIndex, styles, startIndex, oldStyleData.length - endIndex);
	}
	
	/**
	 * Ensures that the "styles" array is the right size for the current "text".
	 * You should only call this if you know that the line requires non-default styling.
	 */
	private short[] maybeResizeStyleData() {
		short[] oldStyleData = styles;
		if (styles == null || styles.length != text.length()) {
			styles = new short[text.length()];
		}
		return oldStyleData;
	}
}
