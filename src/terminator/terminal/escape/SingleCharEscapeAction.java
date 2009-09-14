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

	public void perform(TerminalModel model) {
		switch (escChar) {
			case 'M':  // Move cursor up one line, scrolling if it reaches the top of scroll region.  Opposite of NL.
				model.scrollDisplayUp();
				break;
				
			// Change character set.
			// Note that these are different to the related ^N and ^O sequences, which select character sets 1 and 0 and are handled elsewhere.
			// These sequences ("^[n" and "^[o") are even less common than their relatives.
			case 'n':
				control.invokeCharacterSet(2);
				break;
			case 'o':
				control.invokeCharacterSet(3);
				break;
				
			case '|':
			case '}':
			case '~':
				// Invoke the G3, G2, and G1 character sets as
				// GR. Has no visible effect.
				break;
			default:
				Log.warn("Unrecognized single-character escape \"" + escChar + "\".");
		}
	}
	
	private String getType() {
		switch (escChar) {
		case 'D': return "Down one line";
		case 'E': return "Move to start of next line";
		case 'H': return "Set tab at cursor";
		case 'M': return "Cursor up one line";
		case 'Z': return "Send device attributes (obsolete)";
		case 'n': return "Invoke character set 2";
		case 'o': return "Invoke character set 3";
		case '|':
		case '}':
		case '~': return "Invoke G3, G2, G1 character sets as GR";
		default: return "Unrecognized:" + escChar;
		}
	}
	
	public String toString() {
		return "SingleCharEscapeAction[" + getType() + "]";
	}
	
	private void unsupported(String description) {
		Log.warn("Unsupported single-character escape \"" + escChar + "\" (" + description + ").");
	}
}
