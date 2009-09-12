package terminator;

import e.gui.*;
import e.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import terminator.view.*;

/**
 * Provides a menu bar for Mac OS, and acts as a source of Action instances for
 * the pop-up menu on all platforms.
 */
public class TerminatorMenuBar extends EMenuBar {
	private static int defaultKeyStrokeModifiers = GuiUtilities.getDefaultKeyStrokeModifier();
	
	public TerminatorMenuBar() {
		add(makeFileMenu());
		add(makeHelpMenu());
	}
	
	private JMenu makeFileMenu() {
		JMenu menu = GuiUtilities.makeMenu("File", 'F');
		menu.add(new NewShellAction());
		
		menu.addSeparator();
		menu.add(new NewShellTabAction());
		
		menu.addSeparator();
		menu.add(new CloseAction());
		
		menu.addSeparator();
		menu.add(new ResetAction());
		
		return menu;
	}

	private JMenu makeHelpMenu() {
		HelpMenu helpMenu = new HelpMenu();
		return helpMenu.makeJMenu();
	}
	
	/**
	 * Tests whether the given event corresponds to a keyboard equivalent.
	 */
	public static boolean isKeyboardEquivalent(KeyEvent event) {
		// Windows seems to use ALT_MASK|CTRL_MASK instead of ALT_GRAPH_MASK.
		// We don't want those events, despite the lax comparison later.
		final int fakeWindowsAltGraph = InputEvent.ALT_MASK | InputEvent.CTRL_MASK;
		if ((event.getModifiers() & fakeWindowsAltGraph) == fakeWindowsAltGraph) {
			return false;
		}
		// This comparison is more inclusive than you might expect.
		// If the default modifier is alt, say, we still want to accept alt+shift.
		final int expectedModifiers = defaultKeyStrokeModifiers;
		return ((event.getModifiers() & expectedModifiers) == expectedModifiers);
	}
	
	/**
	 * Returns the appropriate keystroke modifiers for terminal-related actions.
	 */
	public static int getDefaultKeyStrokeModifiers() {
		return defaultKeyStrokeModifiers;
	}
	
	/**
	 * Sets the appropriate keystroke modifiers for terminal-related actions.
	 * On Mac OS, the command key is spare anyway, but on Linux and Windows we'd normally use control.
	 * That's no good in a terminal emulator, because we need to be able to pass things like control-c through.
	 */ 
	public static void setDefaultKeyStrokeModifiers(int modifiers) {
		defaultKeyStrokeModifiers = modifiers;
	}
	
	// Semi-duplicated from GuiUtilities so we can apply our custom modifiers if needed.
	// Use the GuiUtilities version for actions that aren't terminal-related, to get the system-wide defaults.
	private static KeyStroke makeKeyStroke(String key) {
		return GuiUtilities.makeKeyStrokeWithModifiers(defaultKeyStrokeModifiers, key);
	}
	
	private static Component getFocusedComponent() {
		return KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
	}
	
	private static JTerminalPane getFocusedTerminalPane() {
		return (JTerminalPane) SwingUtilities.getAncestorOfClass(JTerminalPane.class, getFocusedComponent());
	}
	
	public static TerminatorFrame getFocusedTerminatorFrame() {
		return (TerminatorFrame) SwingUtilities.getAncestorOfClass(TerminatorFrame.class, getFocusedComponent());
	}
	
	//
	// Any new Action should probably subclass one of these abstract
	// classes. Only if your action requires neither a frame nor a
	// terminal pane (i.e. acts upon the application as a whole) should
	// you subclass AbstractAction directly.
	//
	
	/**
	 * Superclass for actions that need a JTerminalPane (that may or may
	 * not have a frame to itself).
	 */
	private abstract static class AbstractPaneAction extends AbstractAction {
		public AbstractPaneAction(String name) {
			super(name);
		}
		
		public void actionPerformed(ActionEvent e) {
			JTerminalPane terminalPane = getFocusedTerminalPane();
			if (terminalPane != null) {
				performPaneAction(terminalPane);
			}
		}
		
		protected abstract void performPaneAction(JTerminalPane terminalPane);
		
		@Override
		public boolean isEnabled() {
			return (getFocusedTerminatorFrame() != null);
		}
	}
	
	//
	// Terminator's Actions.
	//
	
	public static class NewShellAction extends AbstractAction {
		public NewShellAction() {
			super("New Shell");
			putValue(ACCELERATOR_KEY, makeKeyStroke("N"));
		}
		
		public void actionPerformed(ActionEvent e) {
			Terminator.getSharedInstance().openFrame(JTerminalPane.newShell());
		}
	}
	
	private static void addTab(JTerminalPane terminalPane) {
		TerminatorFrame frame = getFocusedTerminatorFrame();
		
		// On Mac OS, if the user hits C-T multiple times in quick succession while we've got no window up, we end up here with no focused frame.
		// Without this hack, that means that (on my machine) for three C-Ts, I get two in one new window and a third in a second new window.
		// With this hack, the focus even seems to work itself out in the end.
		// I don't know of any cleaner way to do this.
		Frames frames = Terminator.getSharedInstance().getFrames();
		if (frame == null && frames.isEmpty() == false) {
			frame = (TerminatorFrame) frames.getFrame();
		}
		
		if (frame != null) {
			frame.addTab(terminalPane);
		} else {
			// There's no existing frame, so interpret "New Shell Tab..." as "New Shell...".
			Terminator.getSharedInstance().openFrame(terminalPane);
		}
	}
	
	public static class NewShellTabAction extends AbstractAction {
		public NewShellTabAction() {
			super("New Shell Tab");
			putValue(ACCELERATOR_KEY, makeKeyStroke("T"));
		}
		
		public void actionPerformed(ActionEvent e) {
			addTab(JTerminalPane.newShell());
		}
	}
	
	public static class CloseAction extends AbstractPaneAction {
		public CloseAction() {
			super("Close");
			putValue(ACCELERATOR_KEY, TerminatorMenuBar.makeKeyStroke("W"));
			GnomeStockIcon.configureAction(this);
		}
		
		@Override
		protected void performPaneAction(JTerminalPane terminalPane) {
			terminalPane.doCheckedCloseAction();
		}
	}
	
	public static class ResetAction extends AbstractPaneAction {
		public ResetAction() {
			super("Reset");
		}
		
		@Override
		protected void performPaneAction(JTerminalPane terminalPane) {
			terminalPane.reset();
		}
	}
}
