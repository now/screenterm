package terminator.menu;

import e.gui.HelpMenu;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import terminator.menu.*;
import terminator.util.*;

public class MenuBar extends JMenuBar {
  private static int defaultKeyStrokeModifiers =
    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

  public MenuBar(Frame frame) {
    add(new FileMenu(frame));
    if (OS.isMacOs())
      add(WindowMenu.getSharedInstance().add(frame));
    add(new HelpMenu().makeJMenu());
  }

  public static boolean isKeyboardEquivalent(KeyEvent event) {
    // Windows seems to use ALT_MASK|CTRL_MASK instead of ALT_GRAPH_MASK.  We
    // don't want those events, despite the lax comparison later.
    final int fakeWindowsAltGraph = InputEvent.ALT_MASK | InputEvent.CTRL_MASK;
    if ((event.getModifiers() & fakeWindowsAltGraph) == fakeWindowsAltGraph)
      return false;
    // This comparison is more inclusive than you might expect.  If the default
    // modifier is alt, say, we still want to accept alt+shift.
    return (event.getModifiers() & defaultKeyStrokeModifiers) ==
           defaultKeyStrokeModifiers;
  }

  public static void setDefaultKeyStrokeModifiers(int modifiers) {
    defaultKeyStrokeModifiers = modifiers;
  }

  static KeyStroke makeKeyStroke(String key) {
    try {
      return KeyStroke.getKeyStroke(KeyEvent.class.
                                      getField("VK_" + key).
                                      getInt(KeyEvent.class),
                                    defaultKeyStrokeModifiers);
    } catch (Exception e) {
      Log.warn("Couldn’t find virtual keycode for “" + key + "”.", e);
    }
    return null;
  }
}
