package terminator.terminal.states;

import terminator.terminal.*;
import terminator.terminal.actions.*;
import terminator.terminal.charactersets.*;

public class GroundState extends State {
        private static GroundState instance = new GroundState();

        private static StringBuilder characters = new StringBuilder();
        private static CharacterSet characterSet = new NormalCharacterSet();

        public static State enter() {
                return instance;
        }

        public static State enter(ActionQueue actions, char c) {
                return instance.process(actions, c);
        }

        public static void setCharacterSet(CharacterSet newCharacterSet) {
                characterSet = newCharacterSet;
        }

        public static void flush(ActionQueue actions) {
                if (characters.length() == 0)
                        return;
                actions.add(new AddText(characterSet.encode(characters.toString())));
                characters = new StringBuilder();
        }

        public State process(ActionQueue actions, char c) {
                if (c >= 0x20) {
                        characters.append(c);
                        return this;
                }
                flush(actions);
                return super.process(actions, c);
        }
}
