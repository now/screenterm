package terminator.util;

import java.text.*;
import java.io.*;
import java.util.*;

class LogWriter {
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
      log("Couldn’t redirect logging to “" + name + "”", t);
    }
  }

  public void log(final String message, final Throwable t) {
    out.println(String.format("%s %s: %s",
                              DateFormat.format(new Date()), name, message));
    if (t == null)
      return;
    out.println("Associated exception:");
    t.printStackTrace(out);
  }
}
