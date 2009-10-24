package terminator.terminal.actions;

import e.util.*;
import java.awt.Color;

import terminator.model.*;
import terminator.terminal.*;

public class ModifyStyle implements TerminalAction {
        private Color foreground;
        private Color background;
        private Boolean reverseVideo;
        private Boolean underline;

        public void foreground(Color foreground) {
                this.foreground = foreground;
        }

        public void clearForeground() {
                foreground = Style.DEFAULT_FOREGROUND;
        }

        public void background(Color background) {
                this.background = background;
        }

        public void clearBackground() {
                background = Style.DEFAULT_BACKGROUND;
        }

        public void underline(boolean underline) {
                this.underline = Boolean.valueOf(underline);
        }

        public void reverseVideo(boolean reverseVideo) {
                this.reverseVideo = Boolean.valueOf(reverseVideo);
        }

        public void perform(TerminalModelModifier model) {
                model.setStyle(model.getStyle().modify(foreground, background, underline, reverseVideo));
        }
        
        public String toString() {
                StringBuilder string = new StringBuilder();
                appendColorString(string, foreground, "foreground");
                appendColorString(string, background, "background");
                appendBooleanString(string, underline, "underline");
                appendBooleanString(string, reverseVideo, "reverse video");
                return string.toString();
        }

        private void appendColorString(StringBuilder string, Color color, String name) {
                if (color == null)
                        return;
                if (string.length() > 0)
                        string.append(", ");
                string.append("Set ");
                string.append(name);
                string.append(" color to ");
                string.append(color.toString());
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