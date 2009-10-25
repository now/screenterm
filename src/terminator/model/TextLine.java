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

        private LinkedList<StyledText> segments = new LinkedList<StyledText>();

	public TextLine() {
		clear();
	}

        public List<StyledText> styledTexts() {
                return segments;
        }

        public String getString() {
                StringBuilder result = new StringBuilder();
                for (StyledText text : segments)
                        result.append(text.getText());
		return result.toString().replace(TAB_START, ' ').replace(TAB_CONTINUE, ' ');
        }

        public int length() {
                int result = 0;
                for (StyledText text : segments)
                        result += text.length();
                return result;
        }

        public void clear() {
                segments.clear();
        }

        public void killText(int from, int to) {
                if (from >= to)
                        return;
                ListIterator<StyledText> i = segments.listIterator(0);
                int seen = moveTo(from, i);
                killText(i, from - seen, to - seen);
        }

        private void killText(ListIterator<StyledText> i, int from, int to) {
                killSegment(i, 0, killMiddle(i, killSegment(i, from, to)));
        }

        private int killMiddle(ListIterator<StyledText> i, int remaining) {
                while (i.hasNext()) {
                        StyledText text = i.next();
                        if (remaining <= text.length()) {
                                i.previous();
                                break;
                        }
                        i.remove();
                        remaining -= text.length();
                }
                return remaining;
        }

        private int killSegment(ListIterator<StyledText> i, int from, int to) {
                if (!i.hasNext())
                        return 0;
                StyledText segment = i.next();
                i.remove();
                StyledText remainder = segment.removeRange(from, to);
                if (remainder != StyledText.EMPTY)
                        i.add(remainder);
                return (to - from) - (segment.length() - remainder.length());
        }

        private void addRemainder(ListIterator<StyledText> i, StyledText text, int from, int to) {
                StyledText remainder = text.removeRange(from, to);
                if (remainder == null)
                        return;
                i.add(remainder);
        }

        public void insertTextAt(int offset, String text, Style style) {
                ListIterator<StyledText> i = segments.listIterator(0);
                int seen = moveTo(offset, i);
                insertTextAt(i, offset - seen, text, style);
        }

        private void insertTextAt(ListIterator<StyledText> i, int offset, String text, Style style) {
                if (!(insertBefore(i, offset, text, style) ||
                      insertAfter(i, offset, text, style)))
                        insertInside(i, offset, text, style);
        }

        private boolean insertBefore(ListIterator<StyledText> i, int offset, String text, Style style) {
                if (offset != 0)
                        return false;
                i.add(new StyledText(text, style));
                return true;
        }

        private boolean insertAfter(ListIterator<StyledText> i, int offset, String text, Style style) {
                if (i.hasNext())
                        return false;
                char[] padding = new char[offset];
                Arrays.fill(padding, ' ');
                i.add(new StyledText(new String(padding), Style.DEFAULT));
                i.add(new StyledText(text, style));
                return true;
        }

        private void insertInside(ListIterator<StyledText> i, int offset, String text, Style style) {
                StyledText segment = i.next();
                i.remove();
                i.add(segment.removeRange(offset, segment.length()));
                i.add(new StyledText(text, style));
                i.add(segment.removeRange(0, offset));
        }

        public void writeTextAt(int offset, String text, Style style) {
                killText(offset, offset + text.length());
                insertTextAt(offset, text, style);
        }

        private int moveTo(int at, ListIterator<StyledText> i) {
                int seen = 0;
                while (i.hasNext()) {
                        StyledText text = i.next();
                        if (seen <= at && at < seen + text.length()) {
                                i.previous();
                                break;
                        }
                        seen += text.length();
                }
                return seen;
        }

	public void insertTabAt(int offset, int tabLength, Style style) {
		insertTextAt(offset, getTabString(tabLength), style);
	}

	private static String getTabString(int tabLength) {
		char[] tab = new char[tabLength];
		tab[0] = TAB_START;
		Arrays.fill(tab, 1, tab.length, TAB_CONTINUE);
		return new String(tab);
	}
}
