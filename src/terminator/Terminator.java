package terminator;

import com.apple.eawt.*;
import e.gui.*;
import e.util.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import terminator.view.*;

public class Terminator {
	private static final Terminator INSTANCE = new Terminator();
	
	private Frames frames = new Frames();
	
	public static Terminator getSharedInstance() {
		return INSTANCE;
	}
	
	private Terminator() {
		initMacOsEventHandlers();
	}
	
	private void initMacOsEventHandlers() {
		if (GuiUtilities.isMacOs() == false) {
			return;
		}
		
		Application.getApplication().addApplicationListener(new ApplicationAdapter() {
			@Override
			public void handleReOpenApplication(ApplicationEvent e) {
				if (frames.isEmpty()) {
					openFrame(JTerminalPane.newShell());
				}
				e.setHandled(true);
			}
			
			@Override
			public void handleOpenFile(ApplicationEvent e) {
				SimpleDialog.showAlert(null, "Received 'open file' AppleEvent", e.toString());
				Log.warn("open file " + e.toString());
			}
			
			@Override
			public void handleQuit(ApplicationEvent e) {
                                e.setHandled(frames.closeAll());
			}
		});
	}
	
	private void startTerminatorServer() {
		InetAddress loopbackAddress = null;
		try {
			loopbackAddress = InetAddress.getByName(null);
		} catch (UnknownHostException ex) {
			Log.warn("Problem looking up the loopback address", ex);
		}
		new InAppServer("Terminator", System.getProperty("org.jessies.terminator.serverPortFileName"), loopbackAddress, TerminatorServer.class, new TerminatorServer());
	}
	
	/**
	 * Returns whether we did whatever was requested.
	 */
	private boolean parseOriginalCommandLine(final List<String> arguments) {
		PrintWriter out = new PrintWriter(System.out);
		PrintWriter err = new PrintWriter(System.err);
		try {
			TerminatorOpener opener = new TerminatorOpener(arguments, err);
			if (opener.showUsageIfRequested(out)) {
				// Exit with success and without starting the UI or the TerminatorServer.
				return true;
			}
			// We're already on the EDT.
			TerminatorFrame window = opener.createUi();
			if (window == null) {
				// Any syntax error will have been reported but we should still exit with failure,
				// but only after our "finally" clause.
				return false;
			}
			startTerminatorServer();
			// We have no need to wait for the window to be closed.
		} finally {
			out.flush();
			err.flush();
			// In the TerminatorServer case, by contrast, the Ruby has to handle this.
			// The existing Terminator won't have the right DESKTOP_STARTUP_ID.
			GuiUtilities.finishGnomeStartup();
		}
		return true;
	}
	
	public TerminatorFrame openFrame(JTerminalPane terminalPane) {
		TerminatorFrame frame = new TerminatorFrame(terminalPane);
                frames.addFrame(frame);
                return frame;
	}
	
	private void optionsDidChange() {
		// On the Mac, the Command key (called 'meta' by Java) is always used for keyboard equivalents.
		// On other systems, Control tends to be used, but in the special case of terminal emulators this conflicts with the ability to type control characters.
		// The traditional work-around has always been to use Alt, which -- conveniently for Mac users -- is in the same place on a PC keyboard as Command on a Mac keyboard.
		// Things are complicated if we want to support emacs(1), which uses the alt key as meta.
		// Things are complicated in a different direction if we want to support input methods that use alt.
		// At the moment, we assume that Linux users who want characters not on their keyboard will switch keyboard mapping dynamically (which works fine).
		// We can avoid the question on Mac OS for now because disabling input methods doesn't currently work properly, and we don't get the key events anyway.
		if (GuiUtilities.isMacOs() == false) {
			int modifiers = KeyEvent.ALT_MASK;
			TerminatorMenuBar.setDefaultKeyStrokeModifiers(modifiers);
			// When useAltAsMeta is true, we want Alt-F to go to Emacs.
			// When useAltAsMeta is false, we want Alt-F to invoke the Find action.
			// In neither case do we want Alt-F to open the File menu.
			GuiUtilities.setMnemonicsEnabled(false);
		}
	}
	
	public static void main(final String[] argumentArray) {
                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                                try {
                                        GuiUtilities.initLookAndFeel();
                                        getSharedInstance().optionsDidChange();

                                        if (!getSharedInstance().parseOriginalCommandLine(Arrays.asList(argumentArray)))
                                                System.exit(1);
                                } catch (Throwable t) {
                                        Log.warn("Couldn’t start Terminator.", t);
                                        System.exit(1);
                                }
                        }
                });
	}
}
