package terminator.terminal;

class TerminalCharacterSet {
        private int characterSet = 0;
        private char[] g = { 'B', '0' };

        public void invoke(int index) {
                this.characterSet = index;
        }

	public void designate(int index, char set) {
		g[index] = set;
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

	/**
	 * Translate ASCII to the nearest Unicode characters to the special
	 * graphics and line drawing characters.
	 * 
	 * Run this in xterm(1) for reference:
	 * 
	 *   ruby -e 'cs="abcdefghijklmnopqrstuvwxyz"; puts(cs); \
	 *            print("\x1b(0\x1b)B\x0f");puts(cs);print("\x0e")'
	 * 
	 * Or try test 3 of vttest.
	 * 
	 * We use the Unicode box-drawing characters, but the characters
	 * extend out of the bottom of the font's bounding box, spoiling
	 * the effect. Bug parade #4896465.
	 * 
	 * Konsole initially used fonts but switched to doing actual drawing
	 * because of this kind of problem. (Konsole has a menu item to run
	 * a new instance of mc(1), so they need this.)
	 */
	private char translateToGraphicalCharacterSet(char ch) {
		switch (ch) {
		case '`':
			return '\u2666'; // BLACK DIAMOND SUIT
		case 'a':
			return '\u2591'; // LIGHT SHADE
		case 'b':
			return '\u2409'; // SYMBOL FOR HORIZONTAL TABULATION
		case 'c':
			return '\u240c'; // SYMBOL FOR FORM FEED
		case 'd':
			return '\u240d'; // SYMBOL FOR CARRIAGE RETURN
		case 'e':
			return '\u240a'; // SYMBOL FOR LINE FEED
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
		case 'v':
			return '\u2534'; // BOX DRAWINGS LIGHT UP AND HORIZONTAL
		case 'w':
			return '\u252c'; // BOX DRAWINGS LIGHT DOWN AND HORIZONTAL
		case 'o':
		case 'p':
		case 'q':
		case 'r':
		case 's':
			// These should all be different characters,
			// but Unicode only offers one of them.
			return '\u2500'; // BOX DRAWINGS LIGHT HORIZONTAL
		case 't':
			return '\u251c'; // BOX DRAWINGS LIGHT VERTICAL AND RIGHT
		case 'u':
			return '\u2524'; // BOX DRAWINGS LIGHT VERTICAL AND LEFT
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
			return '\u00b7'; // MIDDLE DOT
		default:
			return ch;
		}
	}
}
