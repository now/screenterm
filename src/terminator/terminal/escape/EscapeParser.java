package terminator.terminal.escape;

import java.util.*;
import e.util.*;

import terminator.terminal.*;
import terminator.terminal.actions.*;

public class EscapeParser {
	private boolean isComplete = false;
	private String sequence = "";

	private SequenceRecognizer seqRecognizer;
	
	private static final HashMap<Character, SequenceRecognizer> SEQ_RECOGNIZERS = new HashMap<Character, SequenceRecognizer>();
	static {
		addSequenceRecognizers("M", new SingleCharSequenceRecognizer());
		addSequenceRecognizers("[", new CSISequenceRecognizer());
	}
	private static void addSequenceRecognizers(String chars, SequenceRecognizer recognizer) {
		for (int i = 0; i < chars.length(); i++) {
			SEQ_RECOGNIZERS.put(chars.charAt(i), recognizer);
		}
	}
	
	public void addChar(char ch) {
		sequence += ch;
		if (sequence.length() == 1) {
			seqRecognizer = SEQ_RECOGNIZERS.get(ch);
			if (seqRecognizer == null) {
				Log.warn("Unable to find escape sequence end recognizer for start char \"" + ch + "\"");
			}
		}
		isComplete = (seqRecognizer == null) ? true : seqRecognizer.isAtEnd(sequence);
	}
	
	public boolean isComplete() {
		return isComplete;
	}
	
	public TerminalAction getAction(TerminalControl terminalControl) {
		return (seqRecognizer == null) ? null : seqRecognizer.getTerminalAction(terminalControl, sequence);
	}
	
	public String toString() {
		return sequence;
	}
	
	private interface SequenceRecognizer {
		public boolean isAtEnd(String sequence);
		public TerminalAction getTerminalAction(TerminalControl terminalControl, String sequence);
	}
	
	private static class SingleCharSequenceRecognizer implements SequenceRecognizer {
		public boolean isAtEnd(String sequence) {
			return (sequence.length() == 1);
		}
		
		public TerminalAction getTerminalAction(TerminalControl terminalControl, String sequence) {
                        char escChar = sequence.charAt(0);
                        switch (escChar) {
                        case 'M':
                                return new ScrollDisplayUp();
                        default:
                                Log.warn("Unrecognized single-character escape \"" + escChar + "\".");
                                return null;
                        }
		}
	}
	
	private static class CSISequenceRecognizer implements SequenceRecognizer {
		public boolean isAtEnd(String sequence) {
			if (sequence.length() == 1) {
				return false;
			}
			char endChar = sequence.charAt(sequence.length() - 1);
			return (endChar < ' ' || endChar >= '@');
		}
		
		public TerminalAction getTerminalAction(TerminalControl terminalControl, String sequence) {
			return new CSIEscapeAction(terminalControl, sequence);
		}
	}
}
