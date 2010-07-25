package terminator.menu;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

class JWindowMenu extends JMenu {
  private final Frame frame;

  public JWindowMenu(Frame frame) {
    super("Window");
    this.frame = frame;
  }

  public JWindowMenu update(Set<Frame> windows) {
    removeAll();
    addStandardItems(windows);
    addWindowItems(windows);
    return this;
  }

  private void addStandardItems(Set<Frame> windows) {
    add(new MinimizeAction());
    add(new ZoomAction());
    addSeparator();
    add(new BringAllToFrontAction(windows));
  }

  private void addWindowItems(Set<Frame> windows) {
    if (windows.isEmpty()) {
      disableAll();
      return;
    }
    addSeparator();
    for (Frame window : windows)
      addWindowItem(window);
  }

  private void disableAll() {
    for (int i = 0; i < getItemCount(); ++i)
      disableItem(i);
  }

  private void disableItem(int i) {
    JMenuItem item = getItem(i);
    if (item != null)
      item.setEnabled(false);
  }

  private void addWindowItem(Frame frame) {
    JCheckBoxMenuItem item = new JCheckBoxMenuItem(new FocusWindowAction(frame));
    item.setSelected(frame.isFocused());
    add(item);
  }

  private class MinimizeAction extends FrameAction {
    MinimizeAction() {
      super("Minimize", "M", frame);
    }

    protected void frameActionPerformed(Frame frame) {
      frame.setExtendedState(frame.getExtendedState() | Frame.ICONIFIED);
    }
  }

  private class ZoomAction extends FrameAction {
    ZoomAction() {
      super("Zoom", frame);
    }

    protected void frameActionPerformed(Frame frame) {
      int state = frame.getExtendedState();
      frame.setExtendedState((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH ?
        state ^ Frame.MAXIMIZED_BOTH :
        state | Frame.MAXIMIZED_BOTH);
    }
  }

  private class BringAllToFrontAction extends AbstractAction {
    private final Set<Frame> windows;

    BringAllToFrontAction(Set<Frame> windows) {
      super("Bring All To Front");
      this.windows = windows;
    }

    public void actionPerformed(ActionEvent e) {
      for (Frame frame : windows)
        frame.toFront();
    }

    @Override public boolean isEnabled() {
      return !windows.isEmpty();
    }
  }

  private class FocusWindowAction extends AbstractAction {
    private final Frame frame;

    FocusWindowAction(Frame frame) {
      super(frame.getTitle());
      this.frame = frame;
    }

    public void actionPerformed(ActionEvent e) {
      frame.toFront();
    }
  }
}
