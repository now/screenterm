package terminator;

import java.awt.Color;

public class Palettes {
        /** Tango palette from gnome-terminal 2.24. */
        private static final Color[] TANGO_COLORS = new Color[] {
                new Color(0x2e3436),
                new Color(0xcc0000),
                new Color(0x4e9a06),
                new Color(0xc4a000),
                new Color(0x3465a4),
                new Color(0x75507b),
                new Color(0x06989a),
                new Color(0xd3d7cf),
                new Color(0x555753),
                new Color(0xef2929),
                new Color(0x8ae234),
                new Color(0xfce94f),
                new Color(0x729fcf),
                new Color(0xad7fa8),
                new Color(0x34e2e2),
                new Color(0xeeeeec),
        };

        private static Color[] currentPalette() {
                return TANGO_COLORS;
        }

        /**
         * Returns the color corresponding to 'index' (0-7), in its normal or bright variant.
         */
        public static Color getColor(int index, boolean bright) {
                if (!bright) {
                        return currentPalette()[index];
                } else {
                        return currentPalette()[index + 8];
                }
        }
}
