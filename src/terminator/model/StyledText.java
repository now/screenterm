package terminator.model;

import java.awt.*;

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
        
        public StyledText removeRange(int from, int to) {
                if (from == 0 && to >= text.length())
                        return EMPTY;
                if (from == 0)
                        return new StyledText(text.substring(to), style);
                else if (to >= text.length())
                        return new StyledText(text.substring(0, from), style);
                else
                        return new StyledText(text.substring(0, from) +
                                              text.substring(to), style);
        }
}
