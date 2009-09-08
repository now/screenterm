package terminator.view;

import e.gui.*;
import java.awt.*;

/**
 * Contains JTerminatorPanes and provides their host environment.
 * 
 * Not tied to the rest of Terminator to facilitate embedding.
 */
public interface TerminalPaneHost {
	public void closeTerminalPane(JTerminalPane terminalPane);
}
