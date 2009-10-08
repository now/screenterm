package terminator.model;

public final class Location implements Comparable<Location> {
        private int lineIndex;
        private int charOffset;

        public Location(int lineIndex, int charOffset) {
                this.lineIndex = lineIndex;
                this.charOffset = charOffset;
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
