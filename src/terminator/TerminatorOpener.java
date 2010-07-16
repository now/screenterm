package terminator;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import terminator.util.*;
import terminator.view.*;

public class TerminatorOpener implements Runnable {
  private List<String> arguments;
  private PrintWriter err;
  private TerminatorFrame window;

  public TerminatorOpener(final List<String> arguments, final PrintWriter err) {
    this.arguments = arguments;
    this.err = err;
  }

  private static void showUsage(final PrintWriter out) {
    out.println("Usage: terminator [--help] [[-n <name>] [--working-directory <directory>] [<command>]]...");
  }

  public boolean showUsageIfRequested(final PrintWriter out) {
    if (arguments.contains("-h") || arguments.contains("-help") || arguments.contains("--help")) {
      showUsage(out);
      return true;
    }
    return false;
  }

  /**
   * Sets up the user interface on the AWT event thread.
   */
  public TerminatorFrame openFromBackgroundThread() {
    try {
      EventQueue.invokeAndWait(this);
    } catch (Exception ex) {
      Log.warn("an unexpected checked exception was thrown", ex);
    }
    return window;
  }

  private static class UsageError extends RuntimeException {
    public UsageError(final String message) {
      super(message);
    }
  }

  public TerminatorFrame createUi() {
    try {
      this.window = Terminator.instance().openFrame(JTerminalPane.newShell());
      return window;
    } catch (UsageError ex) {
      err.println(ex.getMessage());
      showUsage(err);
    } catch (Exception ex) {
      err.println(ex.getMessage());
      Log.warn("Failed to open window", ex);
    }
    return null;
  }

  public void run() {
    createUi();
  }
}
