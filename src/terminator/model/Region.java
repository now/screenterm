package terminator.model;

import e.util.*;

class Region {
        private int top;
        private int bottom;

        Region(int top, int bottom) {
                set(top, bottom);
        }

        public void set(int top, int bottom) {
                if (top >= bottom) {
                        Log.warn("Tried to set scrolling region to illegal region " + top + ", " + bottom);
                        return;
                }
                this.top = top;
                this.bottom = bottom;
        }

        public int top() {
                return top;
        }

        public int bottom() {
                return bottom;
        }
}
