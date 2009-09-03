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
        
        addPreference("Behavior", USE_ALT_AS_META, Boolean.FALSE, "Use alt key as meta key (for Emacs)");
    }
}
