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
        Log.warn("Couldn’t start Terminator.", t);
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
    try {
      workAroundSunBug6389282();
      setLookandFeel();
    } catch (Exception e) {
      Log.warn("Problem setting up GUI defaults.", e);
    }
  }

  private void workAroundSunBug6389282() {
    UIManager.getInstalledLookAndFeels();
  }

  private void setLookandFeel() throws Exception {
    String laf = System.getProperty("swing.defaultlaf");
    if (laf == null)
      laf = UIManager.getSystemLookAndFeelClassName();
    UIManager.setLookAndFeel(laf);
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
      Log.warn("Failed to set WM_CLASS.", t);
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
      if (opener.createUi() == null)
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
    } catch (UnknownHostException ex) {
      Log.warn("Problem looking up the loopback address", ex);
    }
    new InAppServer<TerminatorServer>("Terminator",
                                      System.getProperty("org.jessies.terminator.serverPortFileName"),
                                      loopbackAddress,
                                      new TerminatorServer());
  }
}
