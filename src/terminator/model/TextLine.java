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

  public void clear(int from, int count) {
    if (count == 0)
      return;
    ListIterator<StyledText> i = segments.listIterator(0);
    int seen = moveTo(from, i);
    clear(i, from - seen, count);
  }

  private void clear(ListIterator<StyledText> i, int from, int count) {
    clearSegment(i, 0, clearMiddle(i, clearSegment(i, from, count)));
  }

  private int clearMiddle(ListIterator<StyledText> i, int remaining) {
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

  private int clearSegment(ListIterator<StyledText> i, int from, int count) {
    if (!i.hasNext())
      return 0;
    StyledText segment = i.next();
    i.remove();
    StyledText remainder = segment.remove(from, count);
    addIfNotEmpty(i, remainder);
    return count - (segment.length() - remainder.length());
  }

  private void addIfNotEmpty(ListIterator<StyledText> i, StyledText segment) {
    if (segment != StyledText.EMPTY)
      i.add(segment);
  }

  public void clearFrom(int index) {
    clear(index, Integer.MAX_VALUE);
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
    return insertAt(i, text, style);
  }

  private boolean insertAt(ListIterator<StyledText> i, String text, Style style) {
    i.add(new StyledText(text, style));
    return true;
  }

  private boolean insertAfter(ListIterator<StyledText> i, int offset, String text, Style style) {
    if (i.hasNext())
      return false;
    insertPadding(i, offset);
    return insertAt(i, text, style);
  }

  private void insertPadding(ListIterator<StyledText> i, int length) {
    if (length <= 0)
      return;
    i.add(new StyledText(fillString(' ', length), Style.DEFAULT));
  }

  private String fillString(char c, int length) {
    char[] filling = new char[length];
    Arrays.fill(filling, c);
    return new String(filling);
  }

  private void insertInside(ListIterator<StyledText> i, int offset, String text, Style style) {
    StyledText segment = i.next();
    i.remove();
    addIfNotEmpty(i, segment.before(offset));
    insertAt(i, text, style);
    addIfNotEmpty(i, segment.after(offset));
  }

  public void writeTextAt(int offset, String text, Style style) {
    clear(offset, text.length());
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

  public void insertTabAt(int offset, int length, Style style) {
    insertTextAt(offset, tabString(length), style);
  }

  private String tabString(int length) {
    StringBuilder tab = new StringBuilder(length);
    tab.append(TAB_START);
    tab.append(fillString(TAB_CONTINUE, length - 1));
    return tab.toString();
  }
}
