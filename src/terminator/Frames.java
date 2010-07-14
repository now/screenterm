package terminator;

import e.gui.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import terminator.util.*;

public class Frames {
        private ArrayList<TerminatorFrame> list = new ArrayList<TerminatorFrame>();
        private JFrame hiddenMacOSXFrame;

        public TerminatorFrame add(final TerminatorFrame frame) {
                list.add(frame);

                frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowOpened(WindowEvent event) {
                                frameStateChanged();
                        }

                        @Override
                        public void windowClosed(WindowEvent event) {
                                remove(frame);
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

                if (OS.isMacOs())
                        WindowMenu.getSharedInstance().addWindow(frame);

                frameStateChanged();

                return frame;
        }
    
        public void remove(TerminatorFrame frame) {
                list.remove(frame);
                frameStateChanged();
        }
    
        private void frameStateChanged() {
                if (!OS.isMacOs())
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
                        frame.close();
                return true;
        }
}
