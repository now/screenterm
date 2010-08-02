package terminator;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import terminator.util.*;
import terminator.view.*;

public class TerminatorOpener {
  private final List<String> arguments;
  private final PrintWriter err;

  public TerminatorOpener(List<String> arguments, PrintWriter err) {
    this.arguments = arguments;
    this.err = err;
  }

  public boolean showUsageIfRequested(final PrintWriter out) {
    if (!(arguments.contains("-h") || arguments.contains("--help")))
      return false;
    showUsage(out);
    return true;
  }

  private static void showUsage(final PrintWriter out) {
    out.println("Usage: terminator [--help]");
  }

  public TerminatorFrame openFromBackgroundThread() {
    Opener opener = new Opener(err);
    try {
      EventQueue.invokeAndWait(opener);
    } catch (Exception e) {
      Log.warn(e, "an unexpected checked exception was thrown");
    }
    return opener.frame();
  }

  public TerminatorFrame open() {
    return new Opener(err).open();
  }

  private class Opener implements Runnable {
    private final PrintWriter err;
    private TerminatorFrame frame;

    Opener(PrintWriter err) {
      this.err = err;
    }

    public void run() {
      frame = open();
    }

    TerminatorFrame open() {
      try {
        return Terminator.instance().openFrame(JTerminalPane.newShell());
      } catch (Exception e) {
        err.println(e.getMessage());
        Log.warn(e, "failed to open window");
      }
      return null;
    }

    TerminatorFrame frame() {
      return this.frame;
    }
  }
}
