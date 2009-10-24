package terminator;

import java.awt.Color;

public class Palettes {
        private static Color[] COLORS = new Color[256];
        static {
                COLORS[0] = new Color(0x000000);
                COLORS[1] = new Color(0x951616);
                COLORS[2] = new Color(0x257325);
                COLORS[3] = new Color(0x766020);
                COLORS[4] = new Color(0x2f5a9b);
                COLORS[5] = new Color(0x602f80);
                COLORS[6] = new Color(0x5694a8);
                COLORS[7] = new Color(0xc0c0c0);
                COLORS[8] = new Color(0x181818);
                COLORS[9] = new Color(0xf02626);
                COLORS[10] = new Color(0x009000);
                COLORS[11] = new Color(0xf0a500);
                COLORS[12] = new Color(0x2080c0);
                COLORS[13] = new Color(0x933763);
                COLORS[14] = new Color(0x80b0c0);
                COLORS[15] = new Color(0xf6f6f6);
                int i = 16;
                for (int red = 0; red < 6; red++)
                        for (int green = 0; green < 6; green++)
                                for (int blue = 0; blue < 6; blue++)
                                        COLORS[i++] = new Color(red > 0 ? (40 * red + 55) : 0,
                                                                green > 0 ? (40 * green + 55) : 0,
                                                                blue > 0 ? (40 * blue + 55) : 0);
                for (int gray = 0; gray < 24; gray++) {
                        int level = 10 * gray + 8;
                        COLORS[i++] = new Color(level, level, level);
                }
        };

        public static Color getColor(int index) {
                return COLORS[index];
        }
}
