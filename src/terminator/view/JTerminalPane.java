package terminator.view;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

import terminator.*;
import terminator.model.*;
import terminator.terminal.*;
import terminator.util.*;

public class JTerminalPane extends JPanel implements InputHandler {
  private TerminalControl control;
  private TerminalView view;

  public static JTerminalPane newCommandWithArgV(String workingDirectory, List<String> argV) {
    if (argV.size() == 0)
      argV = TerminalControl.getDefaultShell();
    return new JTerminalPane(workingDirectory, argV);
  }

  public static JTerminalPane newShell() {
    return new JTerminalPane(null, TerminalControl.getDefaultShell());
  }

  private JTerminalPane(String workingDirectory, List<String> command) {
    super(new BorderLayout());

    TerminalModel model = new TerminalModel();
    control = new TerminalControl(model);
    view = new TerminalView(model);
    view.addKeyListener(new TerminalInputEncoder(this));

    add(view, BorderLayout.CENTER);

    try {
      control.initProcess(command, workingDirectory);
      initSizeMonitoring();
    } catch (Throwable t) {
      reportTerminalInitializationFailure(t);
    }
  }

  private void reportTerminalInitializationFailure(final Throwable t) {
    Log.warn("Couldn’t initialize terminal", t);
    new Thread() {
      public void run () {
        control.reportFailure("Terminal initialization failed", t);
      }
    }.start();
  }

  public void handleInput(String input) {
    control.send(input);
    view.userIsTyping();
  }

  private void initSizeMonitoring() {
    addComponentListener(new ComponentAdapter() {
      private Dimension currentSize;

      @Override public void componentResized(ComponentEvent event) {
        Dimension size = view.sizeInCharacters();
        if (size.equals(currentSize))
          return;
        control.sizeChanged(size);
        currentSize = size;
      }
    });
  }

  public void start() {
    control.start();
  }

  @Override public void requestFocus() {
    view.requestFocus();
  }

  public void destroyProcess() {
    control.destroyProcess();
  }
}
