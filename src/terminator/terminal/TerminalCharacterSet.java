package terminator.terminal;

class TerminalCharacterSet {
        private int characterSet = 0;
        private char[] g = { 'B', '0' };

        public void invoke(int index) {
                characterSet = index;
        }

	public String translate(String characters) {
		if (g[characterSet] == 'B')
			return characters;

		StringBuilder translation = new StringBuilder(characters.length());
		for (int i = 0; i < characters.length(); ++i)
			translation.append(translateToCharacterSet(characters.charAt(i)));
		return translation.toString();
	}

	private char translateToCharacterSet(char ch) {
		switch (g[characterSet]) {
		case '0':
			return translateToGraphicalCharacterSet(ch);
		default:
			return ch;
		}
	}

	private char translateToGraphicalCharacterSet(char ch) {
		switch (ch) {
                case '+':
                        return '\u2192'; // RIGHTWARDS ARROW
                case ',':
                        return '\u2190'; // LEFTWARDS ARROW
                case '-':
                        return '\u2191'; // UPWARDS ARROW
                case '.':
                        return '\u2193'; // DOWNWARDS ARROW
                case '0':
                        return '\u2588'; // FULL BLOCK
		case '`':
			return '\u2666'; // BLACK DIAMOND SUIT
		case 'a':
			return '\u2591'; // LIGHT SHADE
		case 'f':
			return '\u00b0'; // DEGREE SIGN
		case 'g':
			return '\u00b1'; // PLUS-MINUS SIGN
		case 'h':
			return '\u2424'; // SYMBOL FOR NEW LINE
		case 'i':
			return '\u240b'; // SYMBOL FOR VERTICAL TABULATION
		case 'j':
			return '\u2518'; // BOX DRAWINGS LIGHT UP AND LEFT
		case 'k':
			return '\u2510'; // BOX DRAWINGS LIGHT DOWN AND LEFT
		case 'l':
			return '\u250c'; // BOX DRAWINGS LIGHT DOWN AND RIGHT
		case 'm':
			return '\u2514'; // BOX DRAWINGS LIGHT UP AND RIGHT
		case 'n':
			return '\u253c'; // BOX DRAWINGS LIGHT VERTICAL AND HORIZONTAL
		case 'o':
                        return '\u23ba'; // HORIZONTAL SCAN LINE-1
		case 'p':
                        return '\u23bb'; // HORIZONTAL SCAN LINE-3
		case 'q':
			return '\u2500'; // BOX DRAWINGS LIGHT HORIZONTAL
		case 'r':
                        return '\u23bc'; // HORIZONTAL SCAN LINE-7
		case 's':
			return '\u25bd'; // HORIZONTAL SCAN LINE-9
		case 't':
			return '\u251c'; // BOX DRAWINGS LIGHT VERTICAL AND RIGHT
		case 'u':
			return '\u2524'; // BOX DRAWINGS LIGHT VERTICAL AND LEFT
		case 'v':
			return '\u2534'; // BOX DRAWINGS LIGHT UP AND HORIZONTAL
		case 'w':
			return '\u252c'; // BOX DRAWINGS LIGHT DOWN AND HORIZONTAL
		case 'x':
			return '\u2502'; // BOX DRAWINGS LIGHT VERTICAL
		case 'y':
			return '\u2264'; // LESS-THAN OR EQUAL TO
		case 'z':
			return '\u2265'; // GREATER-THAN OR EQUAL TO
		case '{':
			return '\u03c0'; // GREEK SMALL LETTER PI
		case '|':
			return '\u2260'; // NOT EQUAL TO
		case '}':
			return '\u00a3'; // POUND SIGN
		case '~':
			return '\u2022'; // BULLET
		default:
			return ch;
		}
	}
}
