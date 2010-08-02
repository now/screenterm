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

  public TerminatorFrame openFromBackgroundThread() {
    try {
      EventQueue.invokeAndWait(this);
    } catch (Exception e) {
      Log.warn(e, "an unexpected checked exception was thrown");
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
    } catch (UsageError e) {
      err.println(e.getMessage());
      showUsage(err);
    } catch (Exception e) {
      err.println(e.getMessage());
      Log.warn(e, "failed to open window");
    }
    return null;
  }

  public void run() {
    createUi();
  }
}
