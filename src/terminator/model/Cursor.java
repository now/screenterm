package terminator.model;

import java.awt.Dimension;
import e.util.*;

public final class Cursor {
        private Dimension area;
        private int lineIndex;
        private int charOffset;
        private boolean visible;

        private static int clamp(int value, int min, int max) {
                return Math.max(Math.min(Math.max(min, value), max), 0);
        }

        public static Cursor origo() {
                return new Cursor(new Dimension(0, 0), 0, 0, true);
        }

        private Cursor(Dimension area, int lineIndex, int charOffset, boolean visible) {
                this.area = area;
                this.lineIndex = clamp(lineIndex, 0, area.height - 1);
                this.charOffset = clamp(charOffset, 0, area.width - 1);
        }

        public Cursor constrain(Dimension area) {
                return new Cursor(area, lineIndex, charOffset, visible);
        }

        public Cursor moveToLine(int lineIndex) {
                return new Cursor(area, lineIndex, charOffset, visible);
        }

        public Cursor moveToChar(int charOffset) {
                return new Cursor(area, lineIndex, charOffset, visible);
        }

        public Cursor adjustLineIndex(int delta) {
                return new Cursor(area, lineIndex + delta, charOffset, visible);
        }

        public Cursor adjustCharOffset(int delta) {
                return new Cursor(area, lineIndex, charOffset + delta, visible);
        }

        public Cursor setVisible(boolean visible) {
                return new Cursor(area, lineIndex, charOffset, visible);
        }

        public int getLineIndex() {
                return lineIndex;
        }

        public int getCharOffset() {
                return charOffset;
        }

        public boolean isVisible() {
                return visible;
        }

        public boolean isInsideLines(int first, int last) {
                return visible && (first <= lineIndex && lineIndex <= last);
        }

        public String toString() {
                return "Cursor[line " + lineIndex + ", char " + charOffset + "]";
        }

        // Ought to use a prime, but I can't be bothered to work one out.
        public int hashCode() {
                return (getLineIndex() * 163477) ^ getCharOffset();
        }

        public boolean equals(Object o) {
                if (!(o instanceof Cursor))
                        return false;

                Cursor other = (Cursor)o;
                return other.getLineIndex() == getLineIndex() &&
                       other.getCharOffset() == getCharOffset();
        }
}
