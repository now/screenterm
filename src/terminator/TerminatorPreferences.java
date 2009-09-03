package terminator;

import e.forms.*;
import e.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class TerminatorPreferences extends Preferences {
    public static final String BACKGROUND_COLOR = "background";
    public static final String CURSOR_COLOR = "cursorColor";
    public static final String FOREGROUND_COLOR = "foreground";
    public static final String SELECTION_COLOR = "selectionColor";
    
    public static final String FONT = "font";
    public static final String INITIAL_COLUMN_COUNT = "initialColumnCount";
    public static final String INITIAL_ROW_COUNT = "initialRowCount";
    public static final String SCROLL_ON_KEY_PRESS = "scrollKey";
    public static final String SCROLL_ON_TTY_OUTPUT = "scrollTtyOutput";
    
    /**
     * Whether or not the alt key should be meta.
     * If true, you can't use alt as part of your system's input method.
     * If false, you can't comfortably use emacs(1).
     */
    public static final String USE_ALT_AS_META = "useAltAsMeta";
    
    private static final Color CREAM = new Color(0xfefaea);
    private static final Color LIGHT_BLUE = new Color(0xb3d4ff);
    private static final Color NEAR_BLACK = new Color(0x181818);
    private static final Color NEAR_GREEN = new Color(0x72ff00);
    private static final Color NEAR_WHITE = new Color(0xeeeeee);
    private static final Color SELECTION_BLUE = new Color(0x1c2bff);
    private static final Color VERY_DARK_BLUE = new Color(0x000045);
    
    protected String getPreferencesFilename() {
        return System.getProperty("org.jessies.terminator.optionsFile");
    }
    
    protected void initPreferences() {
        addTab("Behavior");
        addTab("Appearance");
        
        addPreference("Behavior", INITIAL_COLUMN_COUNT, Integer.valueOf(80), "New terminal width");
        addPreference("Behavior", INITIAL_ROW_COUNT, Integer.valueOf(24), "New terminal height");
        addPreference("Behavior", SCROLL_ON_KEY_PRESS, Boolean.TRUE, "Scroll to bottom on key press");
        addPreference("Behavior", SCROLL_ON_TTY_OUTPUT, Boolean.FALSE, "Scroll to bottom on output");
        addPreference("Behavior", USE_ALT_AS_META, Boolean.FALSE, "Use alt key as meta key (for Emacs)");
        
        addPreference("Appearance", FONT, new Font(GuiUtilities.getMonospacedFontName(), Font.PLAIN, 12), "Font");
        
        // Defaults reminiscent of SGI's xwsh(1).
        addPreference("Appearance", BACKGROUND_COLOR, VERY_DARK_BLUE, "Background");
        addPreference("Appearance", CURSOR_COLOR, Color.GREEN, "Cursor");
        addPreference("Appearance", FOREGROUND_COLOR, NEAR_WHITE, "Text foreground");
        addPreference("Appearance", SELECTION_COLOR, SELECTION_BLUE, "Selection background");
    }
}
