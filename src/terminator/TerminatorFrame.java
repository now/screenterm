package terminator;

import e.gui.*;
import e.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import terminator.view.*;

public class TerminatorFrame extends JFrame {
	private JTerminalPane terminal;

	public TerminatorFrame(JTerminalPane initialTerminalPane) {
		super("Terminator");
		terminal = initialTerminalPane;
		initFrame();
		terminal.requestFocus();
                terminal.start();
	}
	
	private void initFrame() {
		JFrameUtilities.setFrameIcon(this);
	
		// Work around Sun bug 6526971 (quick alt-tabbing on Windows can give focus to menu bar).
		if (GuiUtilities.isWindows()) {
			addWindowFocusListener(new WindowAdapter() {
				@Override
				public void windowLostFocus(WindowEvent e) {
					MenuSelectionManager.defaultManager().clearSelectedPath();
				}
			});
		}

                setContentPane(terminal);
                setJMenuBar(new TerminatorMenuBar());
                pack();

                setLocationRelativeTo(null);
                setVisible(true);
	}
	
        public boolean isShowingOnScreen() {
                return isShowing() && (getExtendedState() & ICONIFIED) == 0;
        }
	
        public void close() {
                setVisible(false);
        }

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
                if (visible)
                        return;
                terminal.destroyProcess();
                dispose();
	}
}
