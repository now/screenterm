package terminator;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

import terminator.util.*;
import terminator.view.*;

public class TerminatorFrame extends JFrame {
  private static final Image ICON;
  static {
    String path = System.getProperty("org.jessies.frameIcon");
    Image image = null;
    try {
      if (path != null)
        image = ImageIO.read(new File(path));
    } catch (Throwable t) {
      Log.warn("Failed to load icon “" + path + "”.", t);
    } finally {
      ICON = image;
    }
  } 

  private JTerminalPane terminal;

  public TerminatorFrame(JTerminalPane terminal) {
    super("Terminator");
    this.terminal = terminal;
    setIconImage(ICON);
    setContentPane(terminal);
    setJMenuBar(new TerminatorMenuBar());
    pack();
    workAroundJavaBug6526971();
    setLocationRelativeTo(null);
    setVisible(true);
    terminal.requestFocus();
    terminal.start();
  }

  private void workAroundJavaBug6526971() {
    if (!OS.isWindows())
      return;
    addWindowFocusListener(new WindowAdapter() {
      @Override
      public void windowLostFocus(WindowEvent e) {
        MenuSelectionManager.defaultManager().
      clearSelectedPath();
      }
    });
  }

  public boolean isShowingOnScreen() {
    return isShowing() && (getExtendedState() & ICONIFIED) == 0;
  }

  public void close() {
    setVisible(false);
  }

  @Override
    public void setVisible(boolean visible) {
      super.setVisible(visible);
      if (visible)
        return;
      terminal.destroyProcess();
      dispose();
    }
}
