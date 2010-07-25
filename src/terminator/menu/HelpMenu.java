package terminator.menu;

import com.apple.eawt.*;
import java.awt.event.*;
import javax.swing.*;

import terminator.util.*;

class HelpMenu extends JMenu {
  HelpMenu() {
    super("Help");
    addAboutBox();
  }

  private void addAboutBox() {
    if (OS.isMacOs())
      addAboutBoxToApplicationMenu();
    else
      add(new AboutBoxAction());
  }

  private void addAboutBoxToApplicationMenu() {
    Application.getApplication().addApplicationListener(new ApplicationAdapter() {
      @Override public void handleAbout(ApplicationEvent e) {
        showAboutBox();
        e.setHandled(true);
      }
    });
  }

  private class AboutBoxAction extends StockAction {
    AboutBoxAction() {
      super(OS.isGtk() ? "About" : "About Terminator", "gtk-about");
    }

    public void actionPerformed(ActionEvent e) {
      showAboutBox();
    }
  }

  private void showAboutBox() {
    new AboutBox().setVisible(true);
  }
}
