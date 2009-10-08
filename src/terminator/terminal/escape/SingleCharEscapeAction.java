package terminator.terminal.escape;

import e.util.*;
import terminator.model.*;
import terminator.terminal.*;

/**
 * Recognizes escape sequences consisting of ASCII ESC followed by a single character.
 * Note that most of these are mainly of historical interest, even though some of them look similar to more common sequences.
 */
public class SingleCharEscapeAction implements TerminalAction {
	private TerminalControl control;
	private char escChar;
	
	public SingleCharEscapeAction(TerminalControl control, char escChar) {
		this.control = control;
		this.escChar = escChar;
	}

	public void perform(TerminalModelModifier model) {
		switch (escChar) {
			case 'M':  // Move cursor up one line, scrolling if it reaches the top of scroll region.  Opposite of NL.
				model.scrollDisplayUp();
				break;
			default:
				Log.warn("Unrecognized single-character escape \"" + escChar + "\".");
		}
	}
	
	private String getType() {
		switch (escChar) {
		case 'M': return "Cursor up one line";
		default: return "Unrecognized:" + escChar;
		}
	}
	
	public String toString() {
		return "SingleCharEscapeAction[" + getType() + "]";
	}
}
