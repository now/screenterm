package terminator.model;

public class StyledText {
        public static final StyledText EMPTY = new StyledText("", Style.DEFAULT);

        private String text;
        private Style style;

        public StyledText(String text, Style style) {
                this.text = text;
                this.style = style;
        }
        
        public String getText() {
                return text;
        }
        
        public int length() {
                return text.length();
        }
        
        public Style getStyle() {
                return style;
        }

        public StyledText before(int index) {
                return remove(index, length());
        }

        public StyledText after(int index) {
                return remove(0, index);
        }
        
        public StyledText remove(int from, int count) {
                int after = length() - from;
                if (after <= 0 || count <= 0)
                        return this;
                if (count >= after)
                        return from == 0 ? EMPTY : new StyledText(text.substring(0, from), style);
                int to = from + count;
                if (from == 0)
                        return new StyledText(text.substring(to), style);
                return new StyledText(text.substring(0, from) + text.substring(to), style);
        }
}
