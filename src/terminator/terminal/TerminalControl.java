package terminator.terminal;

import java.awt.Dimension;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import terminator.model.*;
import terminator.terminal.actions.*;
import terminator.terminal.pty.*;
import terminator.terminal.states.*;
import terminator.util.*;

/**
 * Ties together the subprocess reader thread, the subprocess writer thread, and the thread that processes the subprocess' output.
 * Some basic processing is done here.
 */
public class TerminalControl {
  // This should be around your system's pipe size.
  // Too much larger and you'll waste time copying unused char[].
  // Too much smaller and you'll waste time making excessive system calls reading just part of what's available.
  // FIXME: add a JNI call to return PIPE_BUF? (It's not strictly required to be the value we're looking for, but it probably is.)
  private static final int INPUT_BUFFER_SIZE = 8192;

  // We use "new String" here because we're going to use reference equality later to recognize Terminator-supplied defaults.
  private static final String TERMINATOR_DEFAULT_SHELL = new String(System.getenv("SHELL"));

  private TerminalModel model;
  private PTYProcess ptyProcess;

  private ExecutorService writerExecutor;
  private Thread readerThread;

  private State state;
  private ActionQueue actions;

  public static ArrayList<String> getDefaultShell() {
    ArrayList<String> command = new ArrayList<String>();
    command.add(TERMINATOR_DEFAULT_SHELL);
    return command;
  }

  public TerminalControl(TerminalModel model) {
    this.model = model;
    state = GroundState.enter();
    actions = new ActionQueue(model);
  }

  public void initProcess(List<String> command, String workingDirectory) throws Throwable {
    String[] argv = command.toArray(new String[command.size()]);
    String executable = argv[0];
    markAsLoginShellIfDefault(argv);

    ptyProcess = new PTYProcess(executable, argv, workingDirectory);
    writerExecutor = SingleThreadExecutor.create(makeThreadName("Writer"));
  }

  private void markAsLoginShellIfDefault(String[] argv) {
    if (argv[0] == TERMINATOR_DEFAULT_SHELL)
      argv[0] = "-" + argv[0];
  }

  public void destroyProcess() {
    try {
      ptyProcess.destroy();
    } catch (IOException e) {
      Log.warn(e, "failed to destroy process: %s", ptyProcess);
    }
  }

  public void start() {
    if (readerThread != null)
      return;

    if (ptyProcess == null)
      return;

    readerThread = new Thread(new ReaderRunnable(), makeThreadName("Reader"));
    readerThread.setDaemon(true);
    readerThread.start();
  }

  private String makeThreadName(String role) {
    return ptyProcess.name() + " " + role;
  }

  private class ReaderRunnable implements Runnable {
    private char[] chars = new char[INPUT_BUFFER_SIZE];

    public void run() {
      try {
        read();
      } catch (Throwable t) {
        Log.warn(t, "problem reading output from process: %s", ptyProcess);
      } finally {
        handleProcessTermination();
      }
    }

    private void read() throws IOException {
      int n;
      while ((n = ptyProcess.read(chars)) != -1)
        process(n);
      Log.warn("read returned -1 from process: %s", ptyProcess);
    }

    private void process(int n) {
      try {
        processBuffer(chars, n);
      } catch (Throwable t) {
        Log.warn(t, "problem processing output from process: ", ptyProcess);
      }
    }
  }

  private void handleProcessTermination() {
    try {
      ptyProcess.waitFor();
    } catch (Exception e) {
      reportFailure(e, "Problem waiting for process");
      return;
    }
    announceConnectionLost("\n\r[" + ptyProcess.toExitString() + ".]");
  }

  public void reportFailure(Throwable t, String description) {
    StringWriter writer = new StringWriter();
    t.printStackTrace(new PrintWriter(writer));
    announceConnectionLost(writer.toString().replaceAll("\n", "\n\r") +
                           "[" + description + ".]");
  }

  private void announceConnectionLost(String message) {
    try {
      char[] buffer = message.toCharArray();
      processBuffer(buffer, buffer.length);
      model.processActions(new TerminalAction[]{ new SetCursorVisible(false) });
    } catch (Exception e) {
      Log.warn(e, "couldn’t announce connection end: %s", message);
    }
  }

  public void sizeChanged(final Dimension size) {
    model.processActions(new TerminalAction[]{ new TerminalAction() {
      public void perform(TerminalModelModifier model) {
        model.setSize(size);
      }

      public String toString() {
        return "Change terminal size to " + size;
      }
    }});

    try {
      ptyProcess.sendResizeNotification(size);
    } catch (Exception e) {
      Log.warn(e, "couldn’t notify process of size change: %s",  ptyProcess);
    }
  }

  private synchronized void processBuffer(char[] buffer, int size) {
    for (int i = 0; i < size; ++i)
      state = state.process(actions, buffer[i]);
    GroundState.flush(actions);
    actions.flush();
  }

  public void send(final String s) {
    writerExecutor.execute(new Runnable() { public void run() {
      try {
        ptyProcess.write(s);
      } catch (IOException e) {
        Log.warn(e, "couldn’t send to process: %s: %s", ptyProcess, escape(s));
      }
    }});
  }

  private String escape(final String s) {
    final StringBuilder result = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      final char c = s.charAt(i);
      if (c == '\\') {
        result.append("\\\\");
      } else if (c == '\n') {
        result.append("\\n");
      } else if (c == '\r') {
        result.append("\\r");
      } else if (c == '\t') {
        result.append("\\t");
      } else if (c < ' ' || c > '~') {
        String digits = Integer.toString(c, 16);
        result.append("\\u");
        for (int j = 0; j < 4 - digits.length(); i++)
          result.append('0');
        result.append(digits);
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }
}
