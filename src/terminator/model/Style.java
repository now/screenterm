package terminator.model;

import java.awt.*;

import terminator.*;

public class Style {
        public static final Color DEFAULT_FOREGROUND = Palettes.getColor(8);
        public static final Color DEFAULT_BACKGROUND = Palettes.getColor(15);
        public static final Style DEFAULT = new Style(DEFAULT_FOREGROUND, DEFAULT_BACKGROUND, false, false);

        private final Color foreground;
        private final Color background;
        private final boolean underline;
        private final boolean reverseVideo;
        
        public Style(Color foreground, Color background, boolean underline, boolean reverseVideo) {
                this.foreground = foreground;
                this.background = background;
                this.underline = underline;
                this.reverseVideo = reverseVideo;
        }
        
        public Style modify(Color foreground, Color background, Boolean underline, Boolean reverseVideo) {
                return new Style(foreground == null ? this.foreground : foreground,
                                 background == null ? this.background : background,
                                 underline == null ? this.underline : underline,
                                 reverseVideo == null ? this.reverseVideo : reverseVideo);
        }

        public Color foreground() {
                return foreground;
        }
        
        public Color background() {
                return background;
        }
        
        public boolean underline() {
                return underline;
        }
        
        public boolean reverseVideo() {
                return reverseVideo;
        }
}
