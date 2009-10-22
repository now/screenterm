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
import terminator.terminal.escape.*;
import terminator.terminal.actions.*;
import terminator.terminal.charactersets.*;

/**
 * Ties together the subprocess reader thread, the subprocess writer thread, and the thread that processes the subprocess' output.
 * Some basic processing is done here.
 */
public class TerminalControl {
	// Andrew Giddings wanted "windows-1252" for his Psion.
	private static final String CHARSET_NAME = "UTF-8";
	
	// This should be around your system's pipe size.
	// Too much larger and you'll waste time copying unused char[].
	// Too much smaller and you'll waste time making excessive system calls reading just part of what's available.
	// FIXME: add a JNI call to return PIPE_BUF? (It's not strictly required to be the value we're looking for, but it probably is.)
	private static final int INPUT_BUFFER_SIZE = 8192;
	
	// We use "new String" here because we're going to use reference equality later to recognize Terminator-supplied defaults.
	private static final String TERMINATOR_DEFAULT_SHELL = new String(System.getenv("SHELL"));
	
	private static final boolean DEBUG = false;
	private static final boolean DEBUG_STEP_MODE = false;
	private static final boolean SHOW_ASCII_RENDITION = false;
	
	private static BufferedReader stepModeReader;
	
	private TerminalModel model;
	private PtyProcess ptyProcess;
	private boolean processIsRunning;
	
	private InputStreamReader in;
	
	private ExecutorService writerExecutor;
	private Thread readerThread;
	
        private CharacterSet characterSet = new NormalCharacterSet();

	private StringBuilder lineBuffer = new StringBuilder();
	
	private EscapeParser escapeParser;

        private ActionQueue actionQueue;
	
	public TerminalControl(TerminalModel model) {
		this.model = model;
                actionQueue = new ActionQueue(model);
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
		this.ptyProcess = new PtyProcess(executable, argv, workingDirectory);
		this.processIsRunning = true;
		Log.warn("Created " + ptyProcess);
		this.in = new InputStreamReader(ptyProcess.getInputStream(), CHARSET_NAME);
		writerExecutor = ThreadUtilities.newSingleThreadExecutor(makeThreadName("Writer"));
	}
	
	public static ArrayList<String> getDefaultShell() {
		ArrayList<String> command = new ArrayList<String>();
		command.add(TERMINATOR_DEFAULT_SHELL);
		return command;
	}
	
	public void destroyProcess() {
		if (!processIsRunning)
                        return;

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
		return "Process " + ptyProcess.getPid() + " (" + ptyProcess.getPtyName() + ") " + role;
	}
	
	private class ReaderRunnable implements Runnable {
		public void run() {
			try {
				while (true) {
					char[] chars = new char[INPUT_BUFFER_SIZE];
					int readCount = in.read(chars, 0, chars.length);
					if (readCount == -1) {
						Log.warn("read returned -1 from " + ptyProcess);
						return; // This isn't going to fix itself!
					}
					
					try {
						processBuffer(chars, readCount);
					} catch (Throwable th) {
						Log.warn("Problem processing output from " + ptyProcess, th);
					}
				}
			} catch (Throwable th) {
				Log.warn("Problem reading output from " + ptyProcess, th);
			} finally {
				// Our reader might throw an exception before the child has terminated.
				// So "handleProcessTermination" is perhaps not the ideal name.
				handleProcessTermination();
			}
		}
	}
	
        private void invokeCharacterSet(CharacterSet characterSet) {
		flushLineBuffer();
                this.characterSet = characterSet;
	}
	
	private static final void doStep() {
		if (DEBUG_STEP_MODE) {
			try {
				if (stepModeReader == null) {
					stepModeReader = new BufferedReader(new InputStreamReader(System.in));
				}
				stepModeReader.readLine();
			} catch (IOException ex) {
				Log.warn("Problem waiting for stepping input", ex);
			}
		}
	}
	
	private void handleProcessTermination() {
		processIsRunning = false;

		if (writerExecutor != null)
			writerExecutor.shutdownNow();

		if (ptyProcess == null)
			return;

		Log.warn("calling waitFor on " + ptyProcess);
		try {
			ptyProcess.waitFor();
		} catch (Exception ex) {
			Log.warn("Problem waiting for " + ptyProcess, ex);
                        announceConnectionLost(StringUtilities.
                                stackTraceFromThrowable(ex).replaceAll("\n", "\n\r") +
                                "[Problem waiting for process.]");
			return;
		}
		Log.warn("waitFor returned on " + ptyProcess);
		if (ptyProcess.didExitNormally())
                        announceConnectionLost("\n\r[Process exited with status " +
                                               ptyProcess.getExitStatus() +
                                               ".]");
		else if (ptyProcess.wasSignaled())
			announceConnectionLost("\n\r[Process killed by " +
                                               ptyProcess.getSignalDescription() +
                                               ".]");
		else
			announceConnectionLost("\n\r[Lost contact with process.]");
	}
	
	public void announceConnectionLost(String message) {
		try {
			char[] buffer = message.toCharArray();
			processBuffer(buffer, buffer.length);
                        model.processActions(new TerminalAction[] { new TerminalAction() {
                                public void perform(TerminalModelModifier model) {
                                        model.setCursorVisible(false);
                                }
                                
                                public String toString() {
                                        return "TerminalAction[Hide cursor]";
                                }
                        } });
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
	
	private synchronized void processBuffer(char[] buffer, int size) throws IOException {
		for (int i = 0; i < size; ++i)
			processChar(buffer[i]);
		flushLineBuffer();
                actionQueue.flush();
	}
	
	/**
	 * According to vttest, these cursor movement characters are still
	 * treated as such, even when they occur within an escape sequence.
	 */
	private final boolean countsTowardsEscapeSequence(char ch) {
		return (ch != Ascii.BS && ch != Ascii.CR && ch != Ascii.VT);
	}
	
	private synchronized void processChar(final char ch) {
		// Enable this if you're having trouble working out what we're being asked to interpret.
		if (SHOW_ASCII_RENDITION) {
			if (ch >= ' ' || ch == '\n') {
				System.out.print(ch);
			} else {
				System.out.print(".");
			}
		}
		
		if (ch == Ascii.ESC) {
			flushLineBuffer();
			// If the old escape sequence is interrupted; we start a new one.
			if (escapeParser != null) {
				Log.warn("Escape parser discarded with string \"" + escapeParser + "\"");
			}
			escapeParser = new EscapeParser();
			return;
		}
		if (escapeParser != null && countsTowardsEscapeSequence(ch)) {
			escapeParser.addChar(ch);
			if (escapeParser.isComplete()) {
				processEscape();
				escapeParser = null;
			}
		} else if (ch == Ascii.LF || ch == Ascii.CR || ch == Ascii.BS || ch == Ascii.HT || ch == Ascii.VT) {
			flushLineBuffer();
			doStep();
			TerminalAction action = processSpecialCharacter(ch);
                        if (action != null)
                                actionQueue.add(action);
		} else if (ch == Ascii.SO) {
			invokeCharacterSet(new GraphicalCharacterSet());
		} else if (ch == Ascii.SI) {
			invokeCharacterSet(new NormalCharacterSet());
		} else if (ch == Ascii.NUL) {
			// Most telnetd(1) implementations seem to have a bug whereby
			// they send the NUL byte at the end of the C strings they want to
			// output when you first connect. Since all Unixes are pretty much
			// copy and pasted from one another these days, this silly mistake
			// only needed to be made once.
		} else {
			lineBuffer.append(ch);
		}
	}
	
	private synchronized void flushLineBuffer() {
		if (lineBuffer.length() == 0) {
			// Nothing to flush!
			return;
		}
		
		final String line = lineBuffer.toString();
		lineBuffer = new StringBuilder();
		
		doStep();
		
                actionQueue.add(new AddText(characterSet.encode(line)));
	}
	
        private synchronized TerminalAction processSpecialCharacter(final char ch) {
                switch (ch) {
                case Ascii.BS: return new MoveCursorLeft(1);
                case Ascii.HT: return new HorizontalTabulation();
                case Ascii.LF: return new LineFeed();
                case Ascii.VT: return new MoveCursorDown(1);
                case Ascii.CR: return new CarriageReturn();
                default: return null;
                }
	}
	
	public synchronized void processEscape() {
		if (DEBUG) {
			Log.warn("Processing escape sequence \"" + StringUtilities.escapeForJava(escapeParser.toString()) + "\"");
		}
		
		// Invoke all escape sequence handling in the AWT dispatch thread - otherwise we'd have
		// to create billions upon billions of tiny little invokeLater(Runnable) things all over the place.
		doStep();
		TerminalAction action = escapeParser.getAction(this);
		if (action != null) {
                        actionQueue.add(action);
		}
	}
	
	public void sendUtf8String(final String s) {
		writerExecutor.execute(new Runnable() {
			public void run() {
                                if (!processIsRunning)
                                        return;

				try {
                                        OutputStream out = ptyProcess.getOutputStream();
                                        out.write(s.getBytes(CHARSET_NAME));
                                        out.flush();
				} catch (IOException ex) {
					reportFailedSend("string", s, ex);
				}
			}
		});
	}
	
	private void reportFailedSend(String kind, String value, Exception ex) {
		Log.warn("Couldn't send " + kind + " \"" + StringUtilities.escapeForJava(value) + "\" to " + ptyProcess, ex);
	}
}
