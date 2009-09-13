package terminator.view;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import e.gui.*;
import e.util.*;
import org.jessies.os.*;
import terminator.*;
import terminator.model.*;
import terminator.terminal.*;

public class JTerminalPane extends JPanel {
	// The probably over-simplified belief here is that Unix terminals always send ^?.
	// Windows's ReadConsoleInput function always provides applications with ^H, so that's what they expect.
	// Cygwin telnet unhelpfully doesn't translate this to ^?, unlike PuTTY.
	// Cygwin ssh tells the server to expect ^H, which means that backspace works, although the Emacs help is hidden.
	// Search the change log for "backspace" for more information.
	private static final String ERASE_STRING = String.valueOf(GuiUtilities.isWindows() ? Ascii.BS : Ascii.DEL);
	
	private TerminalPaneHost host;
	private TerminalControl control;
	private TerminalView view;
	private JScrollPane scrollPane;
	private JViewport viewport;
	private String name;
	private boolean wasCreatedAsNewShell;
	private Dimension currentSizeInChars;
	private MenuItemProvider menuItemProvider;
	
	/**
	 * Creates a new terminal with the given name, running the given command.
	 */
	private JTerminalPane(String name, String workingDirectory, List<String> command, boolean wasCreatedAsNewShell) {
		super(new BorderLayout());
		this.name = name;
		this.wasCreatedAsNewShell = wasCreatedAsNewShell;
		init(command, workingDirectory);
	}
	
	/**
	 * For XTerm-like "-e" support.
	 */
	public static JTerminalPane newCommandWithArgV(String name, String workingDirectory, List<String> argV) {
		if (argV.size() == 0) {
			argV = TerminalControl.getDefaultShell();
		}
		if (name == null) {
			name = argV.get(0);
		}
		return new JTerminalPane(name, workingDirectory, argV, false);
	}
	
	/**
	 * Creates a new terminal running the given command, with the given
	 * name. If 'name' is null, we use the command as the the name.
	 */
	public static JTerminalPane newCommandWithName(String originalCommand, String name, String workingDirectory) {
		if (name == null) {
			name = originalCommand;
		}
		
		// Avoid having to interpret the command (as java.lang.Process brokenly does) by passing it to the shell as-is.
		ArrayList<String> command = TerminalControl.getDefaultShell();
		command.add("-c");
		command.add(originalCommand);
		
		return new JTerminalPane(name, workingDirectory, command, false);
	}
	
	/**
	 * Creates a new terminal running the user's shell.
	 */
	public static JTerminalPane newShell() {
		return newShellWithName(null, null);
	}
	
	/**
	 * Creates a new terminal running the user's shell with the given name.
	 */
	public static JTerminalPane newShellWithName(String name, String workingDirectory) {
		if (name == null) {
			String user = System.getProperty("user.name");
			name = user + "@localhost";
		}
		return new JTerminalPane(name, workingDirectory, TerminalControl.getDefaultShell(), true);
	}
	
	public Dimension getPaneSize() {
		return viewport.getSize();
	}
	
	public void optionsDidChange() {
		// We're called before start().
		view.optionsDidChange();
		viewport.setBackground(view.getBackground());
		updateTerminalSize();
		scrollPane.invalidate();
		validate();
	}
	
	private void init(List<String> command, String workingDirectory) {
		view = new TerminalView();
		view.addKeyListener(new KeyHandler());
		
		viewport = new JViewport();
		viewport.setView(view);
		
		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setViewport(viewport);
		if (GuiUtilities.isMacOs()) {
			scrollPane.setCorner(JScrollPane.LOWER_RIGHT_CORNER, new FakeScrollBar());
		}
		
		optionsDidChange();
		
		add(scrollPane, BorderLayout.CENTER);
		GuiUtilities.keepMaximumShowing(scrollPane.getVerticalScrollBar());
		
		view.sizeChanged();
		try {
			control = new TerminalControl(view.getModel());
			view.setTerminalControl(control);
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
	
	// On Mac OS there's an ugly hole between the horizontal scroll bar and the grow box.
	// Fill that hole with what looks like an empty horizontal scroll bar track.
	// I don't know how to get a JScrollBar to do the rendering for us, so for now here's a work-around.
	// FIXME: this is broken if Apple change the scroll bar appearance or the user has a high-DPI display.
	// Ideally, I'd have liked to have the bird view to the inside of the vertical scroll bar, encroaching on the terminal's space.
	private static class FakeScrollBar extends JComponent {
		private Color[] colors;
		public Color[] getColors() {
			if (colors == null) {
				int[] pixelColors = new int[] { 0xd4d4d4, 0xd9d9d9, 0xdedede, 0xe5e5e5, 0xe9e9e9, 0xefefef, 0xf3f3f3, 0xf7f7f7, 0xfafafa, 0xfcfcfc, 0xfdfdfd, 0xfdfdfd, 0xfbfbfb, 0xf8f8f8, 0xf5f5f5 };
				colors = new Color[pixelColors.length];
				for (int i = 0; i < pixelColors.length; ++i) {
					colors[i] = new Color(pixelColors[i]);
				}
			}
			return colors;
		}
		public void paintComponent(Graphics oldGraphics) {
			Graphics2D g = (Graphics2D) oldGraphics;
			int x = 0;
			for (Color color : getColors()) {
				g.setColor(color);
				g.drawLine(0, x, getWidth(), x);
				++x;
			}
		}
	};
	
	private void initSizeMonitoring() {
		class SizeMonitor extends ComponentAdapter {
			@Override
			public void componentShown(ComponentEvent event) {
				// Force a size check whenever we're shown in case we're a tab whose window resized while we weren't showing, because in that case we wouldn't have received a componentResized notification.
				componentResized(event);
			}
			
			@Override
			public void componentResized(ComponentEvent event) {
				updateTerminalSize();
			}
		};
		scrollPane.getViewport().addComponentListener(new SizeMonitor());
	}
	
	private void updateTerminalSize() {
		Dimension size = view.getVisibleSizeInCharacters();
		if (size.equals(currentSizeInChars) == false) {
			try {
				control.sizeChanged(size, view.getVisibleSize());
			} catch (Exception ex) {
				if (control != null) {
					Log.warn("Failed to notify " + control.getPtyProcess() + " of size change", ex);
				}
			}
			currentSizeInChars = size;
		}
	}
	
	/** 
	 * Starts the process listening once all the user interface stuff is set up.
	 * 
	 * @param host Reference to the environment hosting this JTerminalPane.
	 */
	public void start(TerminalPaneHost host) {
		this.host = host;
		control.start();
	}
	
	public void reset() {
		control.reset();
	}
	
	private class KeyHandler implements KeyListener {
		public void keyPressed(KeyEvent event) {
			// Leave the event for TerminatorMenuBar if it has appropriate modifiers.
			if (TerminatorMenuBar.isKeyboardEquivalent(event)) {
				return;
			}
			String sequence = getEscapeSequenceForKeyCode(event);
			if (sequence != null) {
				if (sequence.length() == 1) {
					char ch = sequence.charAt(0);
					// We don't get a KEY_TYPED event for the escape key or keypad enter on Mac OS, where we have to handle it in keyPressed.
					// We can't tell the difference between control-tab and control-i in keyTyped, so we have to handle that here too.
					if (ch != Ascii.ESC && ch != Ascii.CR && ch != Ascii.HT) {
						Log.warn("The constraint about not handling keys that generate KEY_TYPED events in keyPressed was probably violated when handling " + event);
					}
				}
				control.sendUtf8String(sequence);
				view.userIsTyping();
				scroll();
				event.consume();
			}
		}

		private String getEscapeSequenceForKeyCode(KeyEvent event) {
			final char keyChar = event.getKeyChar();
			final int keyCode = event.getKeyCode();
			// If this event will be followed by a KEY_TYPED event (that is, has a corresponding Unicode character), you must NOT handle it here; see keyTyped.
			if (keyChar == '\t') {
				// Here's our first awkward case: tab.
				// In keyTyped, we can't tell the difference between control-tab and control-i.
				// We have to handle both here.
				if (event.isControlDown() && keyCode == KeyEvent.VK_TAB) {
					// Control-tab: no corresponding text.
					return null;
				}
				// Plain old tab, or control-i: insert a tab.
				return "\t";
			}
			switch (keyCode) {
				case KeyEvent.VK_ESCAPE:
					// Annoyingly, while Linux sends a KEY_TYPED event for the escape key, Mac OS doesn't.
					return GuiUtilities.isMacOs() ? String.valueOf(Ascii.ESC) : null;
				case KeyEvent.VK_ENTER:
					// Annoyingly, while Linux sends a KEY_TYPED event for the keypad enter, Mac OS doesn't.
					return (GuiUtilities.isMacOs() && event.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD) ? String.valueOf(Ascii.CR) : null;
				
				case KeyEvent.VK_HOME: return editingKeypadSequence(event, 1);
				case KeyEvent.VK_INSERT: return editingKeypadSequence(event, 2);
				case KeyEvent.VK_END: return editingKeypadSequence(event, 4);
				case KeyEvent.VK_PAGE_UP: return editingKeypadSequence(event, 5);
				case KeyEvent.VK_PAGE_DOWN: return editingKeypadSequence(event, 6);
				
				case KeyEvent.VK_UP:
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_RIGHT:
				case KeyEvent.VK_LEFT:
				{
					char letter = "DACB".charAt(keyCode - KeyEvent.VK_LEFT);
					return Ascii.ESC + "[" + oldStyleKeyModifiers(event) + letter;
				}
				
				case KeyEvent.VK_F1:
				case KeyEvent.VK_F2:
				case KeyEvent.VK_F3:
				case KeyEvent.VK_F4:
					// F1-F4 are special cases whose sequences don't look anything like the other F-keys.
					// Beware of out-of-date xterm terminfo in this area.
					// These are the sequences generated by xterm version 224.
					return Ascii.ESC + "O" + oldStyleKeyModifiers(event) + "PQRS".charAt(keyCode - KeyEvent.VK_F1);
				case KeyEvent.VK_F5:
					return functionKeySequence(15, keyCode, KeyEvent.VK_F5, event);
				case KeyEvent.VK_F6:
				case KeyEvent.VK_F7:
				case KeyEvent.VK_F8:
				case KeyEvent.VK_F9:
				case KeyEvent.VK_F10:
					// "ESC[16~" isn't used.
					return functionKeySequence(17, keyCode, KeyEvent.VK_F6, event);
				case KeyEvent.VK_F11:
				case KeyEvent.VK_F12:
					// "ESC[22~" isn't used.
					return functionKeySequence(23, keyCode, KeyEvent.VK_F11, event);
					// The function key codes from here on are VT220 codes.
				case KeyEvent.VK_F13:
				case KeyEvent.VK_F14:
					// Java has a discontinuity between VK_F12 and VK_F13.
					return functionKeySequence(25, keyCode, KeyEvent.VK_F13, event);
				case KeyEvent.VK_F15:
				case KeyEvent.VK_F16:
					// "ESC[27~" isn't used.
					return functionKeySequence(28, keyCode, KeyEvent.VK_F15, event);
					// X11 key codes go up to F35.
					// Java key codes goes up to F24.
					// Escape sequences mentioned in XTerm's "ctlseqs.ms" go up to F20 (VT220).
					// Current Apple keyboards go up to F16, so that's where we stop.
					
				default:
					return null;
			}
		}
		
		private String functionKeySequence(int base, int keyCode, int keyCodeBase, KeyEvent event) {
			int argument = base + (keyCode - keyCodeBase);
			return Ascii.ESC + "[" + argument + functionKeyModifiers(event) + "~";
		}
		
		private String functionKeyModifiers(KeyEvent event) {
			if (event.isShiftDown() && event.isAltDown() && event.isControlDown()) {
				return ";8";
			} else if (event.isAltDown() && event.isControlDown()) {
				return ";7";
			} else if (event.isShiftDown() && event.isControlDown()) {
				return ";6";
			} else if (event.isControlDown()) {
				return ";5";
			} else if (event.isShiftDown() && event.isAltDown()) {
				return ";4";
			} else if (event.isAltDown()) {
				return ";3";
			} else if (event.isShiftDown()) {
				return ";2";
			}
			return "";
		}
		
		private String oldStyleKeyModifiers(KeyEvent event) {
			String modifiers = functionKeyModifiers(event);
			return (modifiers.length() > 0) ? "1" + modifiers : "";
		}
		
		private String editingKeypadSequence(KeyEvent event, int csiDigit) {
			return Ascii.ESC + "[" + csiDigit + functionKeyModifiers(event) + "~";
		}
		
		public void keyReleased(KeyEvent event) {
		}
		
		// Handle key presses which generate keyTyped events.
		private String getUtf8ForKeyEvent(KeyEvent e) {
			char ch = e.getKeyChar();
			if (ch == '\t') {
				// We handled tab in keyPressed because only there can we distinguish control-i and control-tab.
				return null;
			}
			// This modifier test lets Ctrl-H and Ctrl-J generate ^H and ^J instead of
			// mangling them into ^? and ^M.
			// That's useful on those rare but annoying occasions where Backspace and
			// Enter aren't working and it's how other terminals behave.
			if (e.isControlDown() && ch < ' ') {
				return String.valueOf(ch);
			}
			// Work around Sun bug 6320676, and provide support for various terminal eccentricities.
			if (e.isControlDown()) {
				// Control characters are usually typed unshifted, for convenience...
				if (ch >= 'a' && ch <= 'z') {
					return String.valueOf((char) (ch - '`'));
				}
				// ...but the complete range is really from ^@ (ASCII NUL) to ^_ (ASCII US).
				if (ch >= '@' && ch <= '_') {
					return String.valueOf((char) (ch - '@'));
				}
				// There are two special cases that correspond to ASCII NUL.
				// Control-' ' is important for emacs(1).
				if (ch == ' ' || ch == '`') {
					return "\u0000";
				}
				// And one last special case: control-/ is ^_ (ASCII US).
				if (ch == '/') {
					return String.valueOf(Ascii.US);
				}
			}
			if (ch == Ascii.LF) {
				return String.valueOf(Ascii.CR);
			} else if (ch == Ascii.CR) {
				return control.isAutomaticNewline() ? "\r\n" : "\r";
			} else if (ch == Ascii.BS) {
				return ERASE_STRING;
			} else if (ch == Ascii.DEL) {
				return editingKeypadSequence(e, 3);
			} else {
				return String.valueOf(ch);
			}
		}
		
		/**
		 * Handling keyTyped instead of doing everything via keyPressed and keyReleased lets us rely on Sun's translation of key presses to characters.
		 * This includes alt-keypad character composition on Windows.
		 */
		public void keyTyped(KeyEvent event) {
			if (TerminatorMenuBar.isKeyboardEquivalent(event)) {
				event.consume();
				return;
			}
			
			String utf8 = getUtf8ForKeyEvent(event);
			if (utf8 != null) {
				control.sendUtf8String(utf8);
				view.userIsTyping();
				scroll();
				event.consume();
			}
		}
		
		/**
		 * Scrolls the display to the bottom if we're configured to do so.
		 * This should be invoked after any action is performed as a
		 * result of a key press/release/type.
		 */
		public void scroll() {
                        view.scrollToBottomButNotHorizontally();
		}
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
	
	public void doCloseAction() {
		destroyProcess();
		host.closeTerminalPane();
	}
}
