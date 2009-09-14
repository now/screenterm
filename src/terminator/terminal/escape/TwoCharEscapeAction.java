package terminator.terminal.escape;

import e.util.*;
import terminator.model.*;
import terminator.terminal.*;

public class TwoCharEscapeAction implements TerminalAction {
	private TerminalControl control;
	private String sequence;
	
	public TwoCharEscapeAction(TerminalControl control, String sequence) {
		this.control = control;
		this.sequence = sequence;
	}

	public void perform(TerminalModel model) {
		switch (sequence.charAt(0)) {
			case '(':
			case ')':
			case '*':
			case '+':
				control.designateCharacterSet(getCharacterSetIndex(), getCharacterSet());
				break;
			default:
				Log.warn("Unrecognized two-character escape \"" + StringUtilities.escapeForJava(sequence) + "\".");
		}
	}
	
	private int getCharacterSetIndex() {
		return "()*+".indexOf(sequence.charAt(0));
	}
	
	private char getCharacterSet() {
		return sequence.charAt(1);
	}
	
	public String toString() {
		final int characterSetIndex = getCharacterSetIndex();
		if (characterSetIndex != -1) {
			return "TwoCharEscapeAction[Set character set at index " + characterSetIndex + " to " + getCharacterSet() + "]";
		} else {
			return "TwoCharEscapeAction[Unsupported:" + StringUtilities.escapeForJava(sequence) + "]";
		}
	}
}
