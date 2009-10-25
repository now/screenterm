package terminator.terminal;

import e.util.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.swing.event.*;
import terminator.*;
import terminator.model.*;
import terminator.view.*;
import terminator.terminal.actions.*;
import terminator.terminal.charactersets.*;
import terminator.terminal.pty.*;
import terminator.terminal.states.*;

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
	
	public TerminalControl(TerminalModel model) {
		this.model = model;
                state = GroundState.enter();
                actions = new ActionQueue(model);
	}
	
	public void initProcess(List<String> command, String workingDirectory) throws Throwable {
		// We always want to start a login shell.
		// This used to be an option, but it wasn't very useful and it caused confusion.
		// It's also hard to explain the difference without assuming a detailed knowledge of the particular shell.
		// We used to use "--login", but "-l" was more portable.
		// tcsh(1) is so broken that "-l" can only appear on its own.
		// POSIX's sh(1) doesn't even have the notion of login shell (though it does specify vi-compatible line editing).
		// So now we use the 1970s trick of prefixing argv[0] with "-".
		String[] argv = command.toArray(new String[command.size()]);
		String executable = argv[0];
		// We deliberately use reference equality here so we're sure we know what we're meddling with.
		// We only want to modify a call to the user's default shell that Terminator itself inserted into 'command'.
		// If the user's messing about with -e, they get what they ask for no matter what that is.
		// Since we only support -e for compatibility purposes, it's important to have a compatible implementation!
		if (argv[0] == TERMINATOR_DEFAULT_SHELL) {
			argv[0] = "-" + argv[0];
		}
		
		// We log an announceConnectionLost message if we fail to create the PtyProcess, so we need the TerminalLogWriter first.
		this.ptyProcess = new PTYProcess(executable, argv, workingDirectory);
		Log.warn("Created " + ptyProcess);
		writerExecutor = ThreadUtilities.newSingleThreadExecutor(makeThreadName("Writer"));
	}
	
	public static ArrayList<String> getDefaultShell() {
		ArrayList<String> command = new ArrayList<String>();
		command.add(TERMINATOR_DEFAULT_SHELL);
		return command;
	}
	
	public void destroyProcess() {
                try { ptyProcess.destroy(); } catch (IOException ex) {
                        Log.warn("Failed to destroy process " + ptyProcess, ex);
                }
	}
	
	/**
	 * Starts listening to output from the child process. This method is
	 * invoked when all the user interface stuff is set up.
	 */
	public void start() {
		if (readerThread != null)
                        return;

		if (ptyProcess == null)
                        return;

		readerThread = startThread("Reader", new ReaderRunnable());
	}
	
	private Thread startThread(String name, Runnable runnable) {
		Thread thread = new Thread(runnable, makeThreadName(name));
		thread.setDaemon(true);
		thread.start();
		return thread;
	}
	
	private String makeThreadName(String role) {
                return ptyProcess.name() + " " + role;
	}
	
	private class ReaderRunnable implements Runnable {
		public void run() {
			try {
                                char[] chars = new char[INPUT_BUFFER_SIZE];
                                int n;
				while ((n = ptyProcess.read(chars)) != -1) {
					try {
						processBuffer(chars, n);
					} catch (Throwable th) {
						Log.warn("Problem processing output from " + ptyProcess, th);
					}
				}
                                Log.warn("read returned -1 from " + ptyProcess);
			} catch (Throwable th) {
				Log.warn("Problem reading output from " + ptyProcess, th);
			} finally {
				handleProcessTermination();
			}
		}
	}
	
	private void handleProcessTermination() {
		try {
			ptyProcess.waitFor();
		} catch (Exception ex) {
                        announceConnectionLost(StringUtilities.
                                               stackTraceFromThrowable(ex).
                                               replaceAll("\n", "\n\r") +
                                               "[Problem waiting for process.]");
			return;
		}
                announceConnectionLost("\n\r[" + ptyProcess.toExitString() + ".]");
	}
	
	public void announceConnectionLost(String message) {
		try {
			char[] buffer = message.toCharArray();
			processBuffer(buffer, buffer.length);
                        model.processActions(new TerminalAction[]{ new SetCursorVisible(false) });
		} catch (Exception ex) {
			Log.warn("Couldn't say \"" + message + "\"", ex);
		}
	}
	
	/** Must be called in the AWT dispatcher thread. */
	public void sizeChanged(final Dimension size) {
		TerminalAction sizeChangeAction = new TerminalAction() {
			public void perform(TerminalModelModifier model) {
				model.setSize(size);
			}
			
			public String toString() {
				return "TerminalAction[Size change to " + size + "]";
			}
		};
		model.processActions(new TerminalAction[] { sizeChangeAction });

                try {
                        ptyProcess.sendResizeNotification(size);
                } catch (Exception e) {
                        Log.warn("Failed to notify " + ptyProcess + " of size change", e);
                }
	}
	
	private synchronized void processBuffer(char[] buffer, int size) {
		for (int i = 0; i < size; ++i)
                        state = state.process(actions, buffer[i]);
                GroundState.flush(actions);
                actions.flush();
	}
	
	public void sendUtf8String(final String s) {
		writerExecutor.execute(new Runnable() {
			public void run() {
				try {
                                        ptyProcess.write(s);
				} catch (IOException ex) {
                                        Log.warn("Couldn't send string \"" +
                                                 StringUtilities.escapeForJava(s) +
                                                 "\" to " + ptyProcess, ex);
				}
			}
		});
	}
}
