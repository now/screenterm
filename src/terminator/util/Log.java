package terminator.util;

import java.util.regex.*;

public class Log {
  static {
    System.setProperty("sun.awt.exception.handler",
                       "terminator.util.Log$AwtExceptionHandler");
  }

  public static class AwtExceptionHandler {
    public void handle(Throwable t) {
      Log.warn("Exception occurred during event dispatching.", t);
    }
  }

  private static String applicationName =
    System.getProperty("e.util.Log.applicationName", "unknown");
  private static LogWriter out = new LogWriter(applicationName);

  private Log() {
  }

  public static String getApplicationName() {
    return applicationName;
  }

  public static void setApplicationName(String name) {
    applicationName = name;
  }

  public static String getSystemDetailsForProblemReport() {
    return System.getProperty("java.runtime.version") + "/" + os();
  }

  public static void warn(String message) {
    warn(message, null);
  }

  public static void warn(String message, Throwable t) {
    out.log(message, t);
  }

  static {
    warn(version());
    warn(os());
  }

  private static String version() {
    String vm = System.getProperty("java.vm.version");
    String runtime = System.getProperty("java.runtime.version");
    return String.format("Java %s (%s)",
                         System.getProperty("java.version"),
                         vm.equals(runtime) ?
                         vm :
                         String.format("VM %s, runtime %s", vm, runtime));
  }

  private static String os() {
    return String.format("%s %s/%s x%d",
                         System.getProperty("os.name"),
                         launcherOS(System.getProperty("os.version")),
                         System.getProperty("os.arch"),
                         Runtime.getRuntime().availableProcessors());
  }

  private static String launcherOS(String version) {
    String launcher = System.getProperty("e.util.Log.launcherOsVersion");
    if (launcher == null)
      return version;

    Matcher matcher = Pattern.compile("^CYGWIN\\S* ([0-9.]+)").matcher(launcher);
    if (!matcher.find())
      return version;

    return String.format("%s Cygwin %s", version, matcher.group(1));
  }
}
