package terminator.menu;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;

import terminator.util.*;

class WindowMenu {
  private static final WindowMenu INSTANCE = new WindowMenu();

  private final WindowAdapter windowListener = new WindowAdapter() {
    /* TODO: Do we really need to monitor this? */
    @Override public void windowOpened(WindowEvent e) {
      updateMenuWindow();
    }

    @Override public void windowIconified(WindowEvent event) {
      updateMenuWindow();
    }

    @Override public void windowDeiconified(WindowEvent event) {
      updateMenuWindow();
    }

    @Override public void windowGainedFocus(WindowEvent e) {
      update();
    }

    @Override public void windowClosed(WindowEvent e) {
      remove((Frame)e.getWindow());
    }
  };
  private final PropertyChangeListener titleListener = new PropertyChangeListener() {
    @Override public void propertyChange(PropertyChangeEvent e) {
      update();
    }
  };
  private final Map<Frame, JWindowMenu> windows =
    new LinkedHashMap<Frame, JWindowMenu>();
  private JFrame hiddenMacOSXFrame;

  public static WindowMenu getSharedInstance() {
    return INSTANCE;
  }

  private WindowMenu() {
  }

  public JMenu add(Frame frame) {
    if (OS.isMacOs() && frame == hiddenMacOSXFrame)
      return new JWindowMenu(null).update(new LinkedHashSet<Frame>());
    frame.addPropertyChangeListener("title", titleListener);
    frame.addWindowListener(windowListener);
    frame.addWindowFocusListener(windowListener);
    JWindowMenu menu = new JWindowMenu(frame);
    windows.put(frame, menu);
    update();
    return menu;
  }

  private void remove(Frame frame) {
    frame.removePropertyChangeListener("title", titleListener);
    frame.removeWindowListener(windowListener);
    frame.removeWindowFocusListener(windowListener);
    windows.remove(frame);
    update();
  }

  private void update() {
    updateMenuWindow();
    for (JWindowMenu menu : windows.values())
      menu.update(windows.keySet());
  }

  private void updateMenuWindow() {
    if (!OS.isMacOs())
      return;

    boolean noFramesVisible = true;
    for (Frame frame : windows.keySet())
      noFramesVisible = noFramesVisible && !isShowingOnScreen(frame);
    getHiddenMacOSXFrame().setVisible(noFramesVisible);
  }

  private boolean isShowingOnScreen(Frame frame) {
    return frame.isShowing() && (frame.getExtendedState() & Frame.ICONIFIED) == 0;
  }

  private synchronized JFrame getHiddenMacOSXFrame() {
    if (hiddenMacOSXFrame != null)
      return hiddenMacOSXFrame;

    hiddenMacOSXFrame = new JFrame("Mac OS X Hidden Frame");
    hiddenMacOSXFrame.setJMenuBar(new MenuBar(hiddenMacOSXFrame));
    hiddenMacOSXFrame.setUndecorated(true);
    hiddenMacOSXFrame.setLocation(new Point(-100, -100));

    return hiddenMacOSXFrame;
  }
}
