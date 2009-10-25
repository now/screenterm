package terminator.model;

import java.awt.Dimension;

public final class Cursor {
        public static final Cursor ORIGO = new Cursor(new Dimension(0, 0), 0, 0, true);

        private Dimension area;
        private int row;
        private int column;
        private boolean visible;

        private static int clamp(int value, int min, int max) {
                return Math.max(Math.min(Math.max(min, value), max), 0);
        }

        private Cursor(Dimension area, int row, int column, boolean visible) {
                this.area = area;
                this.row = clamp(row, 0, area.height - 1);
                this.column = clamp(column, 0, area.width - 1);
                this.visible = visible;
        }

        public Cursor constrain(Dimension area) {
                return new Cursor(area, row, column, visible);
        }

        public Cursor moveToRow(int row) {
                return new Cursor(area, row, column, visible);
        }

        public Cursor moveToColumn(int column) {
                return new Cursor(area, row, column, visible);
        }

        public Cursor adjustRow(int delta) {
                return new Cursor(area, row + delta, column, visible);
        }

        public Cursor adjustColumn(int delta) {
                return new Cursor(area, row, column + delta, visible);
        }

        public Cursor setVisible(boolean visible) {
                return new Cursor(area, row, column, visible);
        }

        public int getRow() {
                return row;
        }

        public int getColumn() {
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

        // Ought to use a prime, but I can't be bothered to work one out.
        public int hashCode() {
                return (getRow() * 163477) ^ getColumn();
        }

        public boolean equals(Object o) {
                if (!(o instanceof Cursor))
                        return false;

                Cursor other = (Cursor)o;
                return other.getRow() == getRow() &&
                       other.getColumn() == getColumn();
        }
}
