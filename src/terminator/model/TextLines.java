package terminator.model;

import java.awt.Dimension;
import java.util.*;

public class TextLines {
  private LinkedList<TextLine> textLines = new LinkedList<TextLine>();

  public TextLines(Dimension size) {
    setSize(size);
  }

  public void setSize(Dimension size) {
    while (size() > size.height)
      textLines.removeFirst();
    while (size() < size.height)
      textLines.addLast(new TextLine());
  }

  public int insertLines(int beginningAt, int count, int top, int bottom) {
    int above = Math.min(count, bottom - beginningAt + 1);
    replace(beginningAt, bottom + 1, above);
    replace(bottom + 1, top, count - above);
    return above;
  }

  private void replace(int addAt, int removeAt, int count) {
    if (count == 0)
      return;
    add(addAt, count);
    remove(removeAt, count);
  }

  private void add(int at, int count) {
    ListIterator<TextLine> it = textLines.listIterator(at);
    for (int i = 0; i < count; i++)
      it.add(new TextLine());
  }

  private void remove(int at, int count) {
    textLines.subList(at, at + count).clear();
  }

  public void clearFrom(int index) {
    insertLines(index, size() - index, index, size() - 1);
  }

  private int size() {
    return textLines.size();
  }

  public List<TextLine> region(int from, int to) {
    return textLines.subList(from, Math.min(to, size()));
  }

  public TextLine get(int index) {
    return textLines.get(index);
  }
}
