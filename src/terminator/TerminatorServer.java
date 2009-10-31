package terminator;

import e.util.*;
import java.io.*;
import java.util.*;

public class TerminatorServer {
        public void parseCommandLine(PrintWriter out, String line) {
                List<String> arguments = new ArrayList<String>();
                for (String encoded : line.split(" "))
                        arguments.add(StringUtilities.urlDecode(encoded));
                arguments.remove(0);
                TerminatorOpener opener = new TerminatorOpener(arguments, out);
                if (opener.showUsageIfRequested(out))
                        return;
                TerminatorFrame window = opener.openFromBackgroundThread();
                if (window == null)
                        return;
                GuiUtilities.waitForWindowToDisappear(window);
        }
}
