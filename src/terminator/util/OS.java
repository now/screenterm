package terminator.util;

import javax.swing.*;

public class OS {
  private static final boolean isGtk;
  private static final boolean isMacOs;
  private static final boolean isWindows;
  static {
    isGtk = UIManager.getLookAndFeel().getClass().getName().contains("GTK");
    final String osName = System.getProperty("os.name");
    isMacOs = osName.contains("Mac");
    isWindows = osName.contains("Windows");
  }

  public static boolean isGtk() {
    return isGtk;
  }

  public static boolean isMacOs() {
    return isMacOs;
  }

  public static boolean isWindows() {
    return isWindows;
  }
}
