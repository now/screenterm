package terminator;

import e.gui.*;
import e.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/**
 * Ensures that, on Mac OS, we always have our menu bar visible, even
 * when there are no terminal windows open. We use a dummy window with
 * a copy of the menu bar attached. When no other window has the focus,
 * but the application is focused, this hidden window gets the focus,
 * and its menu is used for the screen menu bar.
 */
public class Frames {
        private ArrayList<TerminatorFrame> list = new ArrayList<TerminatorFrame>();
        private JFrame hiddenMacOSXFrame;

        public void addFrame(final TerminatorFrame frame) {
                list.add(frame);

                frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowOpened(WindowEvent event) {
                                frameStateChanged();
                        }

                        @Override
                        public void windowClosed(WindowEvent event) {
                                removeFrame(frame);
                        }

                        @Override
                        public void windowIconified(WindowEvent event) {
                                frameStateChanged();
                        }

                        @Override
                        public void windowDeiconified(WindowEvent event) {
                                frameStateChanged();
                        }
                });

                if (GuiUtilities.isMacOs())
                        WindowMenu.getSharedInstance().addWindow(frame);

                frameStateChanged();
        }
    
        public void removeFrame(TerminatorFrame frame) {
                list.remove(frame);
                if (GuiUtilities.isMacOs())
                        frameStateChanged();
        }
    
        public void frameStateChanged() {
                if (!GuiUtilities.isMacOs())
                        return;

                boolean noFramesVisible = true;
                for (TerminatorFrame frame : list)
                        noFramesVisible = noFramesVisible && !frame.isShowingOnScreen();
                getHiddenMacOSXFrame().setVisible(noFramesVisible);
        }

        private synchronized JFrame getHiddenMacOSXFrame() {
                if (hiddenMacOSXFrame != null)
                        return hiddenMacOSXFrame;

                hiddenMacOSXFrame = new JFrame("Mac OS X Hidden Frame");
                hiddenMacOSXFrame.setJMenuBar(new TerminatorMenuBar());
                hiddenMacOSXFrame.setUndecorated(true);
                hiddenMacOSXFrame.setLocation(new Point(-100, -100));

                return hiddenMacOSXFrame;
        }

        public boolean isEmpty() {
                return list.isEmpty();
        }

        public boolean closeAll() {
                // We need to copy frames as we will be mutating it.
                for (TerminatorFrame frame : new ArrayList<TerminatorFrame>(list))
                        frame.handleWindowCloseRequestFromUser();
                return isEmpty();
        }
}
