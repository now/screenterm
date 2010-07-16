package terminator;

import e.gui.*;
import java.awt.event.*;
import javax.swing.*;

import terminator.util.*;
import terminator.view.*;

public class TerminatorFrame extends JFrame {
  private JTerminalPane terminal;

  public TerminatorFrame(JTerminalPane terminal) {
    super("Terminator");
    this.terminal = terminal;
    JFrameUtilities.setFrameIcon(this);
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
