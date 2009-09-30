package terminator;

import e.gui.*;
import e.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

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
                frameStateChanged();
        }
    
        private void frameStateChanged() {
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
                return true;
        }
}
