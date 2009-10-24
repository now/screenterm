package terminator.terminal.actions;

import e.util.*;

import terminator.model.*;
import terminator.terminal.*;

public class ModifyStyle implements TerminalAction {
        private int foreground;
        private Boolean hasForeground;
        private int background;
        private Boolean hasBackground;
        private Boolean isReverseVideo;
        private Boolean isUnderlined;

        public void foreground(int foreground) {
                this.foreground = foreground;
                hasForeground = Boolean.TRUE;
        }

        public void background(int background) {
                this.background = background;
                hasBackground = Boolean.TRUE;
        }

        public void clearForeground() {
                hasForeground = Boolean.FALSE;
        }

        public void clearBackground() {
                hasBackground = Boolean.FALSE;
        }

        public void reverseVideo(boolean reverseVideo) {
                isReverseVideo = Boolean.valueOf(reverseVideo);
        }

        public void underline(boolean underline) {
                isUnderlined = Boolean.valueOf(underline);
        }

        public void perform(TerminalModelModifier model) {
		int oldStyle = model.getStyle();
		int foreground = StyledText.getForeground(oldStyle);
		boolean hasForeground = StyledText.hasForeground(oldStyle);
		int background = StyledText.getBackground(oldStyle);
		boolean hasBackground = StyledText.hasBackground(oldStyle);
		boolean isReverseVideo = StyledText.isReverseVideo(oldStyle);
		boolean isUnderlined = StyledText.isUnderlined(oldStyle);
                if (this.hasForeground != null) {
                        hasForeground = this.hasForeground.booleanValue();
                        if (hasForeground)
                                foreground = this.foreground;
                }
                if (this.hasBackground != null) {
                        hasBackground = this.hasBackground.booleanValue();
                        if (hasBackground)
                                background = this.background;
                }
                if (this.isReverseVideo != null)
                        isReverseVideo = this.isReverseVideo.booleanValue();
                if (this.isUnderlined != null)
                        isUnderlined = this.isUnderlined.booleanValue();
		model.setStyle(StyledText.getStyle(foreground, hasForeground,
                                                   background, hasBackground,
                                                   isUnderlined, isReverseVideo));
        }
        
        public String toString() {
                StringBuilder string = new StringBuilder();
                appendColorString(string, hasForeground, foreground, "foreground");
                appendColorString(string, hasBackground, background, "background");
                appendBooleanString(string, isReverseVideo, "reverse video");
                appendBooleanString(string, isUnderlined, "underline");
                return string.toString();
        }

        private void appendColorString(StringBuilder string, Boolean set, int color, String name) {
                if (set == null)
                        return;
                if (string.length() > 0)
                        string.append(", ");
                string.append(set.booleanValue() ? "Set" : "Clear");
                string.append(" ");
                string.append(name);
                string.append(" color");
                if (!set.booleanValue())
                        return;
                string.append(" to ");
                string.append(color);
        }

        private void appendBooleanString(StringBuilder string, Boolean value, String name) {
                if (value == null)
                        return;
                if (string.length() > 0)
                        string.append(", ");
                string.append(value.booleanValue() ? "Enable" : "Disable");
                string.append(" ");
                string.append(name);
        }
}
