package terminator.model;

import java.awt.*;
import terminator.*;

/**
A StyledText represents a string of text with one particular style applied.  It also defines in
its namespace the set of allowed styles, and provides static methods for accessing them.

@author Phil Norman
*/

public class StyledText {
        public static final Color FOREGROUND_COLOR = Palettes.getColor(8);
        public static final Color BACKGROUND_COLOR = Palettes.getColor(15);

	private static final int BACKGROUND_SHIFT = 3;
	private static final int FOREGROUND_MASK = 7;
	private static final int BACKGROUND_MASK = 7 << BACKGROUND_SHIFT;
	
        /* 1 << 6 used to be bold, now it’s free. */
	private static final int IS_UNDERLINED = 1 << 7;
	private static final int IS_REVERSE_VIDEO = 1 << 8;
	
	private static final int HAS_FOREGROUND = 1 << 14;
	private static final int HAS_BACKGROUND = 1 << 15;
	
        public static final StyledText EMPTY = new StyledText("", (short)0);

	private String text;
	private Style style;
	private boolean continueToEnd = false;
	
	public StyledText(String text, short style) {
		this(text, new Style(getForegroundColor(style), getBackgroundColor(style), Boolean.valueOf(isUnderlined(style)), isReverseVideo(style)));
	}
	
	public StyledText(String text, Style style) {
		this.text = text;
		this.style = style;
	}
	
	public void setContinueToEnd(boolean continueToEnd) {
		this.continueToEnd = continueToEnd;
	}
	
	public boolean continueToEnd() {
		return continueToEnd;
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
	
	private static Color getForegroundColor(int style) {
		return getColor(style, getForeground(style), true);
	}
	
	private static Color getBackgroundColor(int style) {
		return getColor(style, getBackground(style), false);
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

	private static Color getColor(int style, int colorIndex, boolean isForeground) {
		boolean hasSpecifiedColor = (isForeground ? hasForeground(style) : hasBackground(style));
		Color result = null;
		if (hasSpecifiedColor == false)
			result = isForeground ? FOREGROUND_COLOR : BACKGROUND_COLOR;
		if (result == null)
			result = Palettes.getColor(colorIndex);
		return result;
	}
	
	public static boolean hasBackground(int style) {
		return ((style & HAS_BACKGROUND) != 0);
	}
	
	public static boolean hasForeground(int style) {
		return ((style & HAS_FOREGROUND) != 0);
	}
	
	public static int getForeground(int style) {
		return hasForeground(style) ? (style & FOREGROUND_MASK) : 0;
	}
	
	public static int getBackground(int style) {
		return hasBackground(style) ? ((style & BACKGROUND_MASK) >> BACKGROUND_SHIFT) : 0;
	}
	
	public static boolean isUnderlined(int style) {
		return (style & IS_UNDERLINED) != 0;
	}
	
	public static boolean isReverseVideo(int style) {
		return (style & IS_REVERSE_VIDEO) != 0;
	}
	
	public static short getDefaultStyle() {
		return getStyle(0, false, 0, false, false, false);
	}
	
	public static short getStyle(int foreground, boolean hasForeground, int background, boolean hasBackground, boolean isUnderlined, boolean isReverseVideo) {
		// It's important that if hasForeground or hasBackground is false, we leave the corresponding color field 0.
		// This ensures that no matter how we arrive at the "default" style, it has the same value.
		return (short) ((isUnderlined ? IS_UNDERLINED : 0) | (hasForeground ? (HAS_FOREGROUND | (foreground & FOREGROUND_MASK)) : 0) | (hasBackground ? (HAS_BACKGROUND | ((background << BACKGROUND_SHIFT) & BACKGROUND_MASK)) : 0) | (isReverseVideo ? IS_REVERSE_VIDEO : 0));
	}
	
	public String getDescription() {
		return "StyledText[foreground=" + style.getForeground() + ",background=" + style.getBackground() + ",bold=" + ",underlined=" + style.isUnderlined() + ",reverse=" + style.isReverseVideo() + "]";
	}
}
