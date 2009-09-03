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
    public static final String FONT = "font";
    public static final String INITIAL_COLUMN_COUNT = "initialColumnCount";
    public static final String INITIAL_ROW_COUNT = "initialRowCount";
    
    /**
     * Whether or not the alt key should be meta.
     * If true, you can't use alt as part of your system's input method.
     * If false, you can't comfortably use emacs(1).
     */
    public static final String USE_ALT_AS_META = "useAltAsMeta";
    
    protected String getPreferencesFilename() {
        return System.getProperty("org.jessies.terminator.optionsFile");
    }
    
    protected void initPreferences() {
        addTab("Behavior");
        addTab("Appearance");
        
        addPreference("Behavior", INITIAL_COLUMN_COUNT, Integer.valueOf(80), "New terminal width");
        addPreference("Behavior", INITIAL_ROW_COUNT, Integer.valueOf(24), "New terminal height");
        addPreference("Behavior", USE_ALT_AS_META, Boolean.FALSE, "Use alt key as meta key (for Emacs)");
        
        addPreference("Appearance", FONT, new Font(GuiUtilities.getMonospacedFontName(), Font.PLAIN, 12), "Font");
    }
}
