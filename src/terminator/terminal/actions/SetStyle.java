package terminator.terminal.actions;

import e.util.*;

import terminator.model.*;
import terminator.terminal.*;

public class SetStyle implements TerminalAction {
        String seq;

        public SetStyle(String seq) {
                this.seq = seq;
        }

        public void perform(TerminalModelModifier model) {
		int oldStyle = model.getStyle();
		int foreground = StyledText.getForeground(oldStyle);
		int background = StyledText.getBackground(oldStyle);
		boolean isReverseVideo = StyledText.isReverseVideo(oldStyle);
		boolean isUnderlined = StyledText.isUnderlined(oldStyle);
		boolean hasForeground = StyledText.hasForeground(oldStyle);
		boolean hasBackground = StyledText.hasBackground(oldStyle);
		String[] chunks = seq.split(";");
		for (String chunk : chunks) {
			int value = (chunk.length() == 0) ? 0 : Integer.parseInt(chunk);
			switch (value) {
			case 0:
				hasForeground = false;
				hasBackground = false;
				isReverseVideo = false;
				isUnderlined = false;
				break;
			case 4:
				isUnderlined = true;
				break;
			case 7:
				isReverseVideo = true;
				break;
			case 24:
				isUnderlined = false;
				break;
			case 27:
				isReverseVideo = false;
				break;
			case 30:
			case 31:
			case 32:
			case 33:
			case 34:
			case 35:
			case 36:
			case 37:
				foreground = value - 30;
				hasForeground = true;
				break;
			case 39:
				hasForeground = false;
				break;
			case 40:
			case 41:
			case 42:
			case 43:
			case 44:
			case 45:
			case 46:
			case 47:
				background = value - 40;
				hasBackground = true;
				break;
			case 49:
				hasBackground = false;
				break;
			default:
				Log.warn("Unknown attribute " + value + " in [" + StringUtilities.escapeForJava(seq));
				break;
			}
		}
		model.setStyle(StyledText.getStyle(foreground, hasForeground, background, hasBackground, isUnderlined, isReverseVideo));
        }
        
        public String toString() {
                return "Set style to " + seq;
        }
}
