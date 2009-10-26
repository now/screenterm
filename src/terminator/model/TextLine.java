package terminator.model;

import java.util.*;

public class TextLine {
	private static final char TAB_START = '\t';
	private static final char TAB_CONTINUE = '\r';

        private LinkedList<StyledText> segments = new LinkedList<StyledText>();

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

        public void clearFrom(int index) {
                killText(index, length());
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
