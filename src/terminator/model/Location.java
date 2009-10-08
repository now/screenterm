package terminator.model;

import java.awt.Dimension;
import e.util.*;

public final class Location implements Comparable<Location> {
        private Dimension area;
        private int lineIndex;
        private int charOffset;

        private static int clamp(int value, int min, int max) {
                return Math.max(Math.min(Math.max(min, value), max), 0);
        }

        Location(Dimension area, int lineIndex, int charOffset) {
                this.area = area;
                this.lineIndex = clamp(lineIndex, 0, area.height - 1);
                this.charOffset = clamp(charOffset, 0, area.width - 1);
        }

        public Location moveToLine(int lineIndex) {
                return new Location(area, lineIndex, charOffset);
        }

        public Location moveToChar(int charOffset) {
                return new Location(area, lineIndex, charOffset);
        }

        public Location adjustLineIndex(int delta) {
                return new Location(area, lineIndex + delta, charOffset);
        }

        public Location adjustCharOffset(int delta) {
                return new Location(area, lineIndex, charOffset + delta);
        }

        public int getLineIndex() {
                return lineIndex;
        }

        public int getCharOffset() {
                return charOffset;
        }

        public String toString() {
                return "Location[line " + lineIndex + ", char " + charOffset + "]";
        }

        // Ought to use a prime, but I can't be bothered to work one out.
        public int hashCode() {
                return (getLineIndex() * 163477) ^ getCharOffset();
        }

        public boolean equals(Object o) {
                if (!(o instanceof Location))
                        return false;

                Location other = (Location)o;
                return other.getLineIndex() == getLineIndex() &&
                       other.getCharOffset() == getCharOffset();
        }

        public boolean charOffsetInRange(int begin, int end) {
                return getCharOffset() >= begin && getCharOffset() < end;
        }

        public int compareTo(Location other) {
                if (other.getLineIndex() != getLineIndex())
                        return getLineIndex() - other.getLineIndex();
                return getCharOffset() - other.getCharOffset();
        }
}
