package terminator.util;

import java.io.*;

public class GnomeStartup {
  public static void stop() {
    String id = System.getProperty("gnome.DESKTOP_STARTUP_ID");
    if (id == null)
      return;
    System.clearProperty("gnome.DESKTOP_STARTUP_ID");
    String gnomeStartup = findSupportBinary("gnome-startup");
    if (gnomeStartup == null)
      return;
    ShellProcess.spawn(gnomeStartup, "stop", id);
  }

  private static String findSupportBinary(String name) {
    File path = new File(System.getProperty("org.jessies.binaryDirectory"),
                         binary(name));
    return path.canExecute() ? path.toString() : null;
  }

  private static String binary(String name) {
    return OS.isWindows() ? name + ".exe" : name;
  }

  private GnomeStartup() {
  }
}
