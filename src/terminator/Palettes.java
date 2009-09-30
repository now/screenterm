package terminator;

import java.awt.Color;

public class Palettes {
        private static final Color[] COLORS = new Color[] {
                new Color(0x000000),
                new Color(0x951616),
                new Color(0x257325),
                new Color(0x766020),
                new Color(0x2f5a9b),
                new Color(0x602f80),
                new Color(0x5694a8),
                new Color(0xc0c0c0),
                new Color(0x181818),
                new Color(0xf02626),
                new Color(0x009000),
                new Color(0xf0a500),
                new Color(0x2080c0),
                new Color(0x933763),
                new Color(0x80b0c0),
                new Color(0xf6f6f6),
        };

        public static Color getColor(int index, boolean bright) {
                return COLORS[index + (bright ? 8 : 0)];
        }
}
