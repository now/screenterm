package terminator.util;

import java.text.*;
import java.io.*;
import java.util.*;

class LogWriter implements Closeable {
  private static final SimpleDateFormat DateFormat =
    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SZ");

  private final String name;
  private PrintWriter out = new PrintWriter(System.err, true);

  public LogWriter(final String name) {
    this.name = name;
    initializeOut();
  }

  private void initializeOut() {
    String path = System.getProperty("e.util.Log.filename");
    if (path == null)
      return;

    try {
      out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path,
                                                                        true),
                                                   "utf-8"),
                            true);
    } catch (Throwable t) {
      log(t, "Couldn’t redirect logging to “" + name + "”");
    }
  }

  public void log(String message) {
    out.format("%s %s: %s%n", DateFormat.format(new Date()), name, message);
  }

  public void log(Throwable cause, String message) {
    log(message);
    out.println("Associated exception:");
    cause.printStackTrace(out);
  }

  public void close() {
    out.close();
  }
}
