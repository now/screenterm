package terminator;

import e.util.GuiUtilities;
import java.io.*;
import java.net.*;
import java.util.*;

public class TerminatorServer {
  public void parseCommandLine(PrintWriter out, String line) {
    List<String> arguments = new ArrayList<String>();
    for (String encoded : line.split(" "))
      arguments.add(decode(encoded));
    arguments.remove(0);
    TerminatorOpener opener = new TerminatorOpener(arguments, out);
    if (opener.showUsageIfRequested(out))
      return;
    TerminatorFrame window = opener.openFromBackgroundThread();
    if (window == null)
      return;
    GuiUtilities.waitForWindowToDisappear(window);
  }

  private String decode(String encoded) {
    try {
      return URLDecoder.decode(encoded, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
}
