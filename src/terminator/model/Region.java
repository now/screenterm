package terminator.model;

class Region {
        private int top;
        private int bottom;

        Region(int top, int bottom) {
                set(top, bottom);
        }

        public void set(int top, int bottom) {
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
