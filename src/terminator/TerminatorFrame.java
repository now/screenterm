package terminator;

import e.gui.*;
import e.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import terminator.view.*;

public class TerminatorFrame extends JFrame {
	private JTerminalPane terminal;
	
	private Timer terminalSizeTimer;
	
	private final Color originalBackground = getBackground();
	
	public TerminatorFrame(JTerminalPane initialTerminalPane) {
		super("Terminator");
		terminal = initialTerminalPane;
		initFrame();
		terminal.requestFocus();
                terminal.start();
	}
	
	private void initFrame() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Misnomer: we add our own WindowListener.
		
		JFrameUtilities.setFrameIcon(this);
		
                addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				handleWindowCloseRequestFromUser();
			}
                });
			
		// Work around Sun bug 6526971 (quick alt-tabbing on Windows can give focus to menu bar).
		if (GuiUtilities.isWindows()) {
			addWindowFocusListener(new WindowAdapter() {
				@Override
				public void windowLostFocus(WindowEvent e) {
					MenuSelectionManager.defaultManager().clearSelectedPath();
				}
			});
		}
		
		initTerminal();
		optionsDidChange();
		
		pack();
                setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void initTerminal() {
		Dimension initialSize = terminal.getSize();

		terminal.invalidate();
		setContentPane(terminal);
		validate();

		terminal.requestFocus();

                /* TODO: I don’t think this will be needed any more. */
		Dimension finalSize = getContentPane().getSize();
		fixTerminalSizesAfterAddingOrRemovingTabbedPane(initialSize, finalSize);
	}
	
	/**
	 * It's okay to call this multiple times, and we deliberately do so whenever the preferences change.
	 * This lets us update suggested keystrokes when the use-alt-as-meta option changes.
	 */
	private void updateMenuBar() {
		// Replace any existing menu bar.
		setJMenuBar(new TerminatorMenuBar());
		
		// Work around Sun bug 4949810 (setJMenuBar doesn't call revalidate/repaint).
		getJMenuBar().revalidate();
	}
	
	/**
	 * Increases the size of the frame based on the amount of space taken
	 * away from the terminal to insert the tabbed pane. The end result
	 * should be that the terminal size remains constant but the window
	 * grows.
	 */
	private void fixTerminalSizesAfterAddingOrRemovingTabbedPane(Dimension initialSize, Dimension finalSize) {
		// GNOME's current default window manager automatically ignores setSize if the window is maximized.
		// Windows doesn't, and that causes us to resize a maximized window to be larger than the display, which is obviously unwanted.
		// This early exit fixes Windows' behavior and doesn't hurt Linux.
		if ((getExtendedState() & MAXIMIZED_BOTH) == MAXIMIZED_BOTH) {
			return;
		}
		
		Dimension size = getSize();
		size.height += (initialSize.height - finalSize.height);
		size.width += (initialSize.width - finalSize.width);
		
		// We dealt above with the case where the window is maximized, but we also have to deal with the case where the window is simply very tall.
		// GNOME and Mac OS will correctly constrain the window for us, but on Windows we have to try to do it ourselves.
		if (GuiUtilities.isWindows()) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
			final int availableVerticalScreenSpace = screenSize.height - screenInsets.top - screenInsets.bottom;
			if (getLocation().y + size.height > availableVerticalScreenSpace) {
				size.height = availableVerticalScreenSpace - getLocation().y;
			}
		}
		
		setSize(size);
	}

        public boolean isShowingOnScreen() {
                return isShowing() && (getExtendedState() & ICONIFIED) == 0;
        }
	
	public void handleWindowCloseRequestFromUser() {
                setVisible(false);
	}
	
	/**
	 * Tidies up after the frame has been hidden.
	 * We can't use a ComponentListener because that's invoked on the EDT, as is handleQuit, which relies on us tidying up while it goes.
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
                if (visible)
                        return;
                terminal.destroyProcess();
                dispose();
	}
	
	public void optionsDidChange() {
		updateMenuBar();
		repaint();
	}
}
