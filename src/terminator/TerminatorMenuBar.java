package terminator;

import e.gui.*;
import e.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import terminator.util.*;
import terminator.view.*;

public class TerminatorMenuBar extends EMenuBar {
        private static int defaultKeyStrokeModifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

	public TerminatorMenuBar() {
		add(makeFileMenu());
                add(WindowMenu.getSharedInstance().makeJMenu(new Action[]{}));
		add(makeHelpMenu());
	}

	private JMenu makeFileMenu() {
		JMenu menu = new JMenu("File");
                menu.setMnemonic('F');
		menu.add(new NewShellAction());
		menu.addSeparator();
		menu.add(new CloseAction());
		return menu;
	}

	private JMenu makeHelpMenu() {
		return new HelpMenu().makeJMenu();
	}

	public static boolean isKeyboardEquivalent(KeyEvent event) {
                // Windows seems to use ALT_MASK|CTRL_MASK instead of
                // ALT_GRAPH_MASK.  We don't want those events, despite the lax
                // comparison later.
		final int fakeWindowsAltGraph = InputEvent.ALT_MASK | InputEvent.CTRL_MASK;
		if ((event.getModifiers() & fakeWindowsAltGraph) == fakeWindowsAltGraph)
			return false;
		// This comparison is more inclusive than you might expect.
                // If the default modifier is alt, say, we still want to accept
                // alt+shift.
                final int expectedModifiers = defaultKeyStrokeModifiers;
		return ((event.getModifiers() & expectedModifiers) == expectedModifiers);
	}

	public static void setDefaultKeyStrokeModifiers(int modifiers) {
		defaultKeyStrokeModifiers = modifiers;
	}

	private static KeyStroke makeKeyStroke(String key) {
                try {
                        return KeyStroke.
                                getKeyStroke(KeyEvent.class.
                                                getField("VK_" + key).
                                                getInt(KeyEvent.class),
                                             defaultKeyStrokeModifiers);
                } catch (Exception e) {
                        Log.warn("Couldn’t find virtual keycode for “" + key + "”.", e);
                }
                return null;
	}

	private static Component getFocusedComponent() {
		return KeyboardFocusManager.
                        getCurrentKeyboardFocusManager().
                        getPermanentFocusOwner();
	}

	public static TerminatorFrame getFocusedTerminatorFrame() {
		return (TerminatorFrame)SwingUtilities.
                        getAncestorOfClass(TerminatorFrame.class,
                                           getFocusedComponent());
	}

	private abstract static class AbstractFrameAction extends AbstractAction {
		public AbstractFrameAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			TerminatorFrame frame = getFocusedTerminatorFrame();
			if (frame != null) {
				performFrameAction(frame);
			}
		}

		protected abstract void performFrameAction(TerminatorFrame frame);

		@Override
		public boolean isEnabled() {
			return (getFocusedTerminatorFrame() != null);
		}
	}

	public static class NewShellAction extends AbstractAction {
		public NewShellAction() {
			super("New Shell");
			putValue(ACCELERATOR_KEY, makeKeyStroke("N"));
		}

		public void actionPerformed(ActionEvent e) {
			Terminator.instance().openFrame(JTerminalPane.newShell());
		}
	}

	public static class CloseAction extends AbstractFrameAction {
		public CloseAction() {
			super("Close");
			putValue(ACCELERATOR_KEY, makeKeyStroke("W"));
			GnomeStockIcon.configureAction(this);
		}

		@Override
		protected void performFrameAction(TerminatorFrame frame) {
                        frame.close();
		}
	}
}
