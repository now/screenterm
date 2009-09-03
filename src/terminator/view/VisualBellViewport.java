package terminator.view;

import e.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
import terminator.*;

/**
 * Implements our visual bell by flashing a transparent rectangle over the
 * terminal content. The first implementation composited a bell in a rounded
 * rectangle like Mac OS' mute symbol, only using the beamed sixteenth notes
 * symbol instead of the loudspeaker symbol. Believe it or not, the current
 * simple implementation looks a lot more appropriate, even if it's not
 * particularly impressive.
 */
public class VisualBellViewport extends JViewport {
    private static final int BELL_VISIBLE_MS = 200;
    
    private boolean isBellVisible = false;
    private Timer timer;
    
    public VisualBellViewport() {
        initTimer();
    }
    
    private void initTimer() {
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setBellVisibility(false);
            }
        };
        timer = new Timer(BELL_VISIBLE_MS, actionListener);
        timer.setRepeats(false);
    }
    
    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (isBellVisible) {
            paintBell(g);
        }
    }
    
    private void paintBell(Graphics g) {
            return;
    }
    
    /**
     * Imitates the effect of alpha-blending the foreground 40% with the background.
     */
    private static final int blend(float back, float fore) {
        return (int)(0.4f * fore + 0.6f * back);
    }
    
    /**
     * Flashes the visual bell.
     * This class has no other public API.
     */
    public void flash() {
        if (GuiUtilities.isMacOs()) {
            // Mac OS has a system-wide "visual bell" that looks much better than ours.
            // You can turn this on in the Universal Access preference pane, by checking "Flash the screen when an alert sound occurs".
            // We can take advantage of Apple's hard work if the user has this turned on by "actually" ringing the bell.
            // This doesn't change our "no noise pollution" policy, if you ignore the very unlikely race condition.
            ArrayList<String> result = new ArrayList<String>();
            ArrayList<String> errors = new ArrayList<String>();
            int status = ProcessUtilities.backQuote(null, new String[] { "defaults", "-currentHost", "read", "Apple Global Domain", "com.apple.sound.beep.flash" }, result, errors);
            if (status == 0 && errors.size() == 0 && result.size() == 1 && result.get(0).equals("1")) {
                Toolkit.getDefaultToolkit().beep();
                return;
            }
        }
        
        setBellVisibility(true);
        timer.restart();
    }
    
    private void setBellVisibility(final boolean newState) {
        isBellVisible = newState;
        ((JComponent) getView()).setOpaque(!isBellVisible);
        repaint();
    }
}
