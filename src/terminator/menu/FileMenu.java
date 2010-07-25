package terminator.menu;

import e.gui.GnomeStockIcon;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import terminator.*;

class FileMenu extends JMenu {
  FileMenu(Frame frame) {
    super("Screen");
    add(new NewAction());
    addSeparator();
    add(new CloseAction(frame));
  }
  
  private class NewAction extends AcceleratableAction {
    NewAction() {
      super("New", "N");
    }

    public void actionPerformed(ActionEvent e) {
      Terminator.instance().openFrame();
    }
  }

  private class CloseAction extends FrameAction {
    CloseAction(Frame frame) {
      super("Close", "W", frame);
      /* TODO: Remove this, or move code here. */
      GnomeStockIcon.configureAction(this);
    }

    protected void frameActionPerformed(Frame frame) {
      frame.setVisible(false);
    }
  }
}
