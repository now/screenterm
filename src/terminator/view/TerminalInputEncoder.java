package terminator.view;

import java.awt.event.*;

import terminator.*;
import terminator.util.*;

class TerminalInputEncoder implements KeyListener {
	// The probably over-simplified belief here is that Unix terminals always send ^?.
	// Windows's ReadConsoleInput function always provides applications with ^H, so that's what they expect.
	// Cygwin telnet unhelpfully doesn't translate this to ^?, unlike PuTTY.
	// Cygwin ssh tells the server to expect ^H, which means that backspace works, although the Emacs help is hidden.
	private static final String ERASE_STRING = String.valueOf(OS.isWindows() ? Ascii.BS : Ascii.DEL);
	
        private InputHandler handler;

        public TerminalInputEncoder(InputHandler handler) {
                this.handler = handler;
        }

        public void keyPressed(KeyEvent e) {
                if (TerminatorMenuBar.isKeyboardEquivalent(e))
                        return;

                switch (e.getKeyCode()) {
                case KeyEvent.VK_ESCAPE: transmitEscape(e); break;
                case KeyEvent.VK_ENTER: transmitEnter(e); break;
                case KeyEvent.VK_HOME: transmitFunction(e, 1); break;
                case KeyEvent.VK_INSERT: transmitFunction(e, 2); break;
                case KeyEvent.VK_END: transmitFunction(e, 3); break;
                case KeyEvent.VK_PAGE_UP: transmitFunction(e, 5); break;
                case KeyEvent.VK_PAGE_DOWN: transmitFunction(e, 6); break;
                case KeyEvent.VK_UP: transmitCursor(e, 'A'); break;
                case KeyEvent.VK_DOWN: transmitCursor(e, 'B'); break;
                case KeyEvent.VK_RIGHT: transmitCursor(e, 'C'); break;
                case KeyEvent.VK_LEFT: transmitCursor(e, 'D'); break;
                case KeyEvent.VK_F1: transmitSpecialFunction(e, 'P'); break;
                case KeyEvent.VK_F2: transmitSpecialFunction(e, 'Q'); break;
                case KeyEvent.VK_F3: transmitSpecialFunction(e, 'R'); break;
                case KeyEvent.VK_F4: transmitSpecialFunction(e, 'S'); break;
                case KeyEvent.VK_F5: transmitFunction(e, 15); break;
                case KeyEvent.VK_F6: transmitFunction(e, 17); break;
                case KeyEvent.VK_F7: transmitFunction(e, 18); break;
                case KeyEvent.VK_F8: transmitFunction(e, 19); break;
                case KeyEvent.VK_F9: transmitFunction(e, 20); break;
                case KeyEvent.VK_F10: transmitFunction(e, 21); break;
                case KeyEvent.VK_F11: transmitFunction(e, 23); break;
                case KeyEvent.VK_F12: transmitFunction(e, 24); break;
                }
        }

        private void transmit(KeyEvent event, String sequence) {
                if (sequence == null)
                        return;

                handler.handleInput(sequence);
                event.consume();
        }

        private void transmit(KeyEvent event, char c) {
                transmit(event, String.valueOf(c));
        }

        private void transmitEscape(KeyEvent event) {
                if (OS.isMacOs())
                        transmit(event, Ascii.ESC);
        }

        private void transmitEnter(KeyEvent event) {
                if (OS.isMacOs() &&
                    event.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD)
                        transmit(event, Ascii.CR);
        }

        private void transmitCursor(KeyEvent event, char c) {
                transmit(event, Ascii.ESC + "[" + c);
        }

        private void transmitSpecialFunction(KeyEvent event, char c) {
                transmit(event, Ascii.ESC + "O" + c);
        }

        private void transmitFunction(KeyEvent event, int digits) {
                transmit(event, Ascii.ESC + "[" + digits + "~");
        }

        public void keyReleased(KeyEvent event) {
        }

        public void keyTyped(KeyEvent event) {
                if (TerminatorMenuBar.isKeyboardEquivalent(event)) {
                        event.consume();
                        return;
                }

                char c = event.getKeyChar();

                if (transmitControl(event))
                        return;

                switch (c) {
                case Ascii.BS: transmit(event, ERASE_STRING); break;
                case Ascii.LF: transmit(event, Ascii.CR); break;
                case Ascii.DEL: transmitFunction(event, 3); break;
                default: transmit(event, c); break;
                }
        }

        private boolean transmitControl(KeyEvent event) {
                if (!event.isControlDown())
                        return false;

                if (event.getKeyChar() < ' ') {
                        transmit(event, event.getKeyChar());
                        return true;
                }

                switch (event.getKeyChar()) {
                case ' ':
                case '`': transmit(event, Ascii.NUL); return true;
                case '/': transmit(event, Ascii.US); return true;
                default: return false;
                }
        }
}
