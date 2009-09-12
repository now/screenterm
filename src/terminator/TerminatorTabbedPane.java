package terminator;

import e.gui.*;
import e.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import terminator.terminal.*;
import terminator.view.*;

public class TerminatorTabbedPane extends TabbedPane {
    static {
        // Normally we use small tabbed panes on Mac OS, but activity indicators and close buttons need more space.
        UIManager.put("TabbedPane.useSmallLayout", Boolean.FALSE);
    }

    public TerminatorTabbedPane() {
        addChangeListener(new TerminalFocuser());
        
        // Mac OS 10.5 defaults JTabbedPanes to non-opaque (though this wasn't in the release notes).
        if (GuiUtilities.isMacOs()) {
            setOpaque(true);
        }
    }
    
    @Override public String getToolTipTextAt(int index) {
        // Unless you have a ridiculous number of tabs, you'll get this bit.
        String switchMessage = "";
        String key = tabIndexToKey(index);
        if (key != null) {
            String primaryModifier = KeyEvent.getKeyModifiersText(TerminatorMenuBar.getDefaultKeyStrokeModifiers()) + "+";
            if (GuiUtilities.isMacOs()) {
                primaryModifier = "\u2318";
            }
            switchMessage = "Use " + primaryModifier + key + " to switch to this tab.<br>";
        }
        
        // You always get this bit.
        String control = GuiUtilities.isMacOs() ? "\u2303" : "Ctrl+";
        String cycleMessage = "Use " + control + "Tab to cycle through the tabs.";
        
        return "<html><body>" + switchMessage + cycleMessage;
    }
    
    @Override public void addTab(String name, Component c) {
        super.addTab(name, c);
        
        int newIndex = getTabCount() - 1;
        Component tabEar = new TerminatorTabComponent((JTerminalPane) c);
        setTabComponentAt_safe(newIndex, tabEar);
    }
    
    @Override protected void fireStateChanged() {
        super.fireStateChanged();
        updateSpinnerVisibilities();
    }
    
    /**
     * Hides the selected tab's spinner.
     */
    private synchronized void updateSpinnerVisibilities() {
        int index = getSelectedIndex();
        if (index != -1) {
            TerminatorTabComponent component = (TerminatorTabComponent) getTabComponentAt_safe(index);
            if (component != null) {
                component.stopActivityDisplay();
            }
        }
    }
    
    private static class TerminatorTabComponent extends JPanel implements ChangeListener {
        private final JTerminalPane terminalPane;
        private final JLabel label;
        private final JAsynchronousProgressIndicator outputSpinner;
        
        private TerminatorTabComponent(JTerminalPane terminalPane) {
            // FIXME: would BoxLayout make more sense?
            super(new BorderLayout(4, 0));
            
            this.terminalPane = terminalPane;
            
            this.label = new JLabel(terminalPane.getName());
            label.setOpaque(false);
            setOpaque(false);
            
            this.outputSpinner = new JAsynchronousProgressIndicator();
            outputSpinner.setDisplayedWhenStopped(true);
            // The ChangeListener will start the spinner if necessary.
            // We need to default to "stopped" in case the user's set "always show tabs".
            stopActivityDisplay();
            terminalPane.getControl().addChangeListener(this);
            
            final JButton closeButton = makeCloseButton();
            
            if (GuiUtilities.isMacOs()) {
                // FIXME: does this look okay?
                add(closeButton, BorderLayout.WEST);
                add(label, BorderLayout.CENTER);
                add(outputSpinner, BorderLayout.EAST);
            } else {
                add(label, BorderLayout.WEST);
                add(outputSpinner, BorderLayout.CENTER);
                add(closeButton, BorderLayout.EAST);
            }
        }
        
        private JButton makeCloseButton() {
            final JButton button = GuiUtilities.isGtk() ? makeGtkCloseButton() : Buttons.makeCloseTabButton();
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    terminalPane.doCheckedCloseAction();
                }
            });
            return button;
        }
        
        private JButton makeGtkCloseButton() {
            final JButton button = new JButton(GnomeStockIcon.getStockIcon("gtk-close", GnomeStockIcon.Size.GTK_ICON_SIZE_MENU));
            // In Firefox and GEdit, the only Gnome applications I know with closeable tabs...
            // ...the border is pulled tight around the icon...
            button.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            // ...and is only visible on rollover.
            button.setContentAreaFilled(false);
            button.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    button.setContentAreaFilled(true);
                }
                
                @Override public void mouseExited(MouseEvent e) {
                    button.setContentAreaFilled(false);
                }
            });
            // We don't want a focus ring, nor do we want to accept the focus.
            button.setFocusPainted(false);
            button.setFocusable(false);
            return button;
        }
        
        public void stopActivityDisplay() {
            outputSpinner.setPainted(false);
        }

        public void stateChanged(ChangeEvent e) {
            if (!terminalPane.isShowing()) {
                outputSpinner.setPainted(true);
                outputSpinner.animateOneFrame();
            }
        }
        
        public void setTitle(String title) {
            label.setText(title);
        }
    }
    
    /**
     * Ensures that when we change tab, we give focus to that terminal.
     */
    private class TerminalFocuser implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            final JTerminalPane selected = (JTerminalPane) getSelectedComponent();
            if (selected != null) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        selected.requestFocus();
                    }
                });
            }
        }
    }
}
