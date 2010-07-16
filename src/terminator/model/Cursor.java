package terminator.model;

import java.awt.Dimension;

public final class Cursor {
  public static final Cursor ORIGO = new Cursor(new Dimension(0, 0),
                                                Region.INITIAL,
                                                0, 0, true);

  private Dimension area;
  private Region region;
  private int row;
  private int column;
  private boolean visible;

  private static int clamp(int value, int min, int max) {
    return Math.max(Math.min(Math.max(min, value), max), 0);
  }

  private Cursor(Dimension area, Region region, int row, int column,
                 boolean visible) {
    this.area = area;
    this.region = region;
    this.row = clamp(row, 0, area.height - 1);
    this.column = clamp(column, 0, area.width - 1);
    this.visible = visible;
  }

  public Cursor constrain(Dimension area, Region region) {
    return new Cursor(area, region, row, column, visible);
  }

  public Cursor constrain(Region region) {
    return new Cursor(area, region, row, column, visible);
  }

  public Cursor moveToRow(int row) {
    return new Cursor(area, region, row, column, visible);
  }

  public Cursor moveToColumn(int column) {
    return new Cursor(area, region, row, column, visible);
  }

  public Cursor adjustRow(int delta) {
    int adjusted = row + delta;
    if (delta < 0 && row >= region.top())
      adjusted = Math.max(adjusted, region.top());
    else if (delta > 0 && row <= region.bottom())
      adjusted = Math.min(adjusted, region.bottom());
    return new Cursor(area, region, adjusted, column, visible);
  }

  public Cursor adjustColumn(int delta) {
    return new Cursor(area, region, row, column + delta, visible);
  }

  public Cursor setVisible(boolean visible) {
    return new Cursor(area, region, row, column, visible);
  }

  public int row() {
    return row;
  }

  public int column() {
    return column;
  }

  public boolean isVisible() {
    return visible;
  }

  public boolean isInsideLines(int first, int last) {
    return visible && (first <= row && row <= last);
  }

  public String toString() {
    return "Cursor[row " + row + ", column " + column + "]";
  }

  public int hashCode() {
    return 31 * (31 * (17 + row) + column);
  }

  public boolean equals(Object o) {
    if (!(o instanceof Cursor))
      return false;

    Cursor other = (Cursor)o;
    return other.row == row && other.column == column;
  }
}
