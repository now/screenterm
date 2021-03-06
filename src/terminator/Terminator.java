package terminator;

import com.apple.eawt.*;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

import terminator.menu.*;
import terminator.util.*;
import terminator.view.*;

public class Terminator {
  private static final Terminator INSTANCE = new Terminator();

  private Frames frames = new Frames();

  public static Terminator instance() {
    return INSTANCE;
  }

  public static void main(final String[] args) {
    EventQueue.invokeLater(new Runnable() { public void run() {
      try {
        instance().initLookAndFeel();
        instance().initInterface();

        if (!instance().parseArgs(Arrays.asList(args)))
          System.exit(1);
      } catch (Throwable t) {
        Log.warn(t, "couldn’t start Terminator");
        System.exit(1);
      }
    }});
  }

  private Terminator() {
    initMacOsEventHandlers();
  }

  private void initMacOsEventHandlers() {
    if (!OS.isMacOs())
      return;

    Application.getApplication().addApplicationListener(new ApplicationAdapter() {
      @Override public void handleReOpenApplication(ApplicationEvent e) {
        if (frames.isEmpty())
          openFrame();
        e.setHandled(true);
      }

      @Override public void handleQuit(ApplicationEvent e) {
        e.setHandled(frames.closeAll());
      }
    });
  }

  public TerminatorFrame openFrame() {
    return openFrame(JTerminalPane.newShell());
  }

  public TerminatorFrame openFrame(JTerminalPane terminalPane) {
    return frames.add(new TerminatorFrame(terminalPane));
  }

  private void initLookAndFeel() {
    workAroundSunBug6389282();
    setLookandFeel();
  }

  private void workAroundSunBug6389282() {
    UIManager.getInstalledLookAndFeels();
  }

  private void setLookandFeel() {
    String laf = System.getProperty("swing.defaultlaf");
    if (laf == null)
      laf = UIManager.getSystemLookAndFeelClassName();
    try {
      UIManager.setLookAndFeel(laf);
    } catch (Exception e) {
      Log.warn(e, "problem setting up GUI defaults");
    }
    setWMClass(laf);
  }

  private void setWMClass(String laf) {
    if (!laf.contains("GTK"))
      return;

    try {
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Field field = toolkit.getClass().getDeclaredField("awtAppClassName");
      field.setAccessible(true);
      field.set(toolkit, Log.getApplicationName());
    } catch (Throwable t) {
      Log.warn(t, "failed to set WM_CLASS");
    }
  }

  private void initInterface() {
    if (OS.isMacOs())
      return;

    MenuBar.setDefaultKeyStrokeModifiers(KeyEvent.ALT_MASK);
  }

  private boolean parseArgs(final List<String> arguments) {
    PrintWriter out = new PrintWriter(System.out);
    PrintWriter err = new PrintWriter(System.err);
    try {
      TerminatorOpener opener = new TerminatorOpener(arguments, err);
      if (opener.showUsageIfRequested(out))
        return true;
      if (opener.open() == null)
        return false;
      startTerminatorServer();
    } finally {
      out.flush();
      err.flush();
      GnomeStartup.stop();
    }
    return true;
  }

  private void startTerminatorServer() {
    InetAddress loopbackAddress = null;
    try {
      loopbackAddress = InetAddress.getByName(null);
    } catch (UnknownHostException e) {
      Log.warn(e, "problem looking up the loopback address");
    }
    new InAppServer<TerminatorServer>("Terminator",
                                      System.getProperty("org.jessies.terminator.serverPortFileName"),
                                      loopbackAddress,
                                      new TerminatorServer());
  }
}
