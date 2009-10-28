package terminator.view;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import e.util.*;

import terminator.*;
import terminator.model.*;
import terminator.terminal.*;

public class JTerminalPane extends JPanel implements InputHandler {
	private TerminalControl control;
	private TerminalView view;
	
	/**
	 * Creates a new terminal with the given name, running the given command.
	 */
	private JTerminalPane(String workingDirectory, List<String> command) {
		super(new BorderLayout());
		init(command, workingDirectory);
	}
	
	/**
	 * For XTerm-like "-e" support.
	 */
	public static JTerminalPane newCommandWithArgV(String workingDirectory, List<String> argV) {
		if (argV.size() == 0)
			argV = TerminalControl.getDefaultShell();
		return new JTerminalPane(workingDirectory, argV);
	}
	
	/**
	 * Creates a new terminal running the user's shell.
	 */
	public static JTerminalPane newShell() {
                return new JTerminalPane(null, TerminalControl.getDefaultShell());
	}
	
	private void init(List<String> command, String workingDirectory) {
                TerminalModel model = new TerminalModel();
		view = new TerminalView(model);
                view.addKeyListener(new TerminalInputEncoder(this));

		add(view, BorderLayout.CENTER);
		
		try {
			control = new TerminalControl(model);
			control.initProcess(command, workingDirectory);
			initSizeMonitoring();
		} catch (final Throwable th) {
			Log.warn("Couldn't initialize terminal", th);
			// We can't call announceConnectionLost off the EDT.
			new Thread(new Runnable() {
				public void run() {
					String exceptionDetails = StringUtilities.stackTraceFromThrowable(th).replaceAll("\n", "\n\r");
					control.announceConnectionLost(exceptionDetails + "[Couldn't initialize terminal: " + th.getClass().getSimpleName() + ".]");
				}
			}).start();
		}
	}

        public void handleInput(String input) {
                control.sendUtf8String(input);
                view.userIsTyping();
        }
	
	private void initSizeMonitoring() {
		addComponentListener(new ComponentAdapter() {
                        private Dimension currentSize;

			@Override
			public void componentResized(ComponentEvent event) {
                                Dimension size = view.sizeInCharacters(getSize());
                                if (size.equals(currentSize))
                                        return;
                                control.sizeChanged(size);
                                currentSize = size;
			}
	
		});
	}
	
	/** 
	 * Starts the process listening once all the user interface stuff is set up.
	 * 
	 * @param host Reference to the environment hosting this JTerminalPane.
	 */
	public void start() {
		control.start();
	}

	/**
	 * Hands focus to our text pane.
	 */
	public void requestFocus() {
		view.requestFocus();
	}

	public void destroyProcess() {
		control.destroyProcess();
	}
}
