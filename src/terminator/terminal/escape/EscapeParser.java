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

                private int parseParameter(String string, int standard) {
                        return string.length() == 0 ? standard : Integer.parseInt(string);
                }

                private int parseCount(String string) {
                        return parseParameter(string, 1);
                }

                private int parseType(String string) {
                        return parseParameter(string, 0);
                }

		public TerminalAction getTerminalAction(TerminalControl terminalControl, String sequence) {
                        char lastChar = sequence.charAt(sequence.length() - 1);
                        String midSequence = sequence.substring(1, sequence.length() - 1);
                        switch (lastChar) {
                        case 'A': return new MoveCursorUp(parseCount(midSequence));
                        case 'B': return new MoveCursorDown(parseCount(midSequence));
                        case 'C': return new MoveCursorRight(parseCount(midSequence));
                        case 'D': return new MoveCursorLeft(parseCount(midSequence));
                        case 'H': {
                                  String seq = midSequence;
                                  int row = 1;
                                  int column = 1;
                                  int splitIndex = seq.indexOf(';');
                                  if (splitIndex != -1) {
                                          row = Integer.parseInt(seq.substring(0, splitIndex));
                                          column = Integer.parseInt(seq.substring(splitIndex + 1));
                                  }
                                  return new PositionCursor(row - 1, column - 1);
                        }
                        case 'K':
                                switch (parseType(midSequence)) {
                                case 0: return new ClearToEndOfLine();
                                case 1: return new ClearToBeginningOfLine();
                                default: Log.warn("Unknown line clearing request " + midSequence); return null;
                                }
                        case 'J':
                                switch (parseType(midSequence)) {
                                case 0: return new ClearToEndOfScreen();
                                default: Log.warn("Unknown screen clearing request " + midSequence); return null;
                                }
                        case 'L': return new InsertLines(parseCount(midSequence));
                        case 'M': return new DeleteLines(parseCount(midSequence));
                        case 'P': return new DeleteCharacters(parseCount(midSequence));
                        case 'h': return setDecPrivateMode(midSequence, true);
                        case 'l': return setDecPrivateMode(midSequence, false);
                        case 'm': return new SetStyle(midSequence);
                        case 'r': {
                                 String seq = midSequence;
                                 int index = seq.indexOf(';');
                                 if (index == -1)
                                         return null;
                                 return new SetScrollingRegion(Integer.parseInt(seq.substring(0, index)) - 1,
                                                               Integer.parseInt(seq.substring(index + 1)) - 1);
                        }
                        default: Log.warn("unknown CSI sequence " + StringUtilities.escapeForJava(sequence)); return null;
                        }
		}

                private TerminalAction setDecPrivateMode(String seq, boolean value) {
                        boolean isPrivateMode = seq.startsWith("?");
                        String[] modes = (isPrivateMode ? seq.substring(1) : seq).split(";");
                        for (String modeString : modes) {
                                int mode = Integer.parseInt(modeString);
                                if (isPrivateMode) {
                                        switch (mode) {
                                        case 25:
                                                return new SetCursorVisible(value);
                                        default:
                                                Log.warn("Unknown private mode " + mode + " in [" + StringUtilities.escapeForJava(seq) + (value ? 'h' : 'l'));
                                        }
                                } else {
                                        switch (mode) {
                                        case 4:
                                                return new SetInsertMode(value);
                                        default:
                                                Log.warn("Unknown mode " + mode + " in [" + StringUtilities.escapeForJava(seq) + (value ? 'h' : 'l'));
                                        }
                                }
                        }
                        return null;
                }
	}
}
