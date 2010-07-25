package terminator.menu;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;

import terminator.util.*;

class AboutBox extends JDialog {
  private String name;
  private String version;
  private String copyright;
  private ImageIcon icon;

  /* TODO: Information should be read from file. */
  AboutBox() {
    super(owner(), "About Terminator");

    name = "Terminator";
    version = "1.0.0";
    copyright = "Copyright © 2010 Nikolai Weibull";
    icon = null;

    setContentPane(new Content());

    pack();
    setMaximumSize(getPreferredSize());
    setMinimumSize(getPreferredSize());
    setResizable(false);

    setLocationRelativeTo(getOwner());

    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    closeOnEsc();
  }

  @Override public void setVisible(boolean visible) {
    super.setVisible(visible);
    if (!visible)
      dispose();
  }

  private static Frame owner() {
    if (OS.isMacOs())
      return null;

    Frame owner = (Frame)SwingUtilities.getAncestorOfClass(Frame.class,
                    KeyboardFocusManager.
                      getCurrentKeyboardFocusManager().
                      getPermanentFocusOwner());
    if (owner != null)
      return owner;

    Frame[] frames = Frame.getFrames();
    if (frames.length > 0)
      return frames[0];
    return null;
  }

  private static final String CLOSE_ACTION_NAME = "terminator.menu.CloseOnEsc";

  private void closeOnEsc() {
    getRootPane().
      getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
      put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
          CLOSE_ACTION_NAME);
    getRootPane().getActionMap().put(CLOSE_ACTION_NAME, new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Window window = SwingUtilities.getWindowAncestor((Component)e.getSource());
        window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
      }
    });
  }

  private class Content extends JPanel {
    private Fonts fonts;

    Content() {
      fonts = Fonts.create();

      /* TODO: Why is this not the same for all OSes? */
      setBorder(BorderFactory.createEmptyBorder(8, 12, OS.isGtk() ? 12 : 20, 12));
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      addIcon();
      addName();
      addVersion();
      addCopyright();
    }

    private void addIcon() {
      if (icon == null)
        return;
      addLabel(new JLabel(icon));
      addSpace(2);
    }

    private void addName() {
      addLabel(fonts.getNameFont(), name);
      addSpace();
    }

    private void addVersion() {
      addLabel(fonts.getVersionFont(), version);
      addSpace();
    }

    private void addCopyright() {
      addLabel(fonts.getCopyrightFont(), copyright);
    }

    private void addSpace() {
      addSpace(1);
    }

    private void addSpace(int factor) {
      add(Box.createRigidArea(new Dimension(1, 8 * factor)));
    }

    private void addLabel(Font font, String text) {
      JLabel label = new JLabel(text);
      label.setFont(font);
      addLabel(label);
    }

    private void addLabel(JComponent label) {
      label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
      add(label);
    }
  }

  private static abstract class Fonts {
    protected Font nameFont;
    protected Font versionFont;

    static Fonts create() {
      if (OS.isGtk())
          return new GtkFonts();
      else if (OS.isWindows())
        return new WindowsFonts();
      else
        return new DefaultFonts();
    }

    public Font getNameFont() {
      return nameFont;
    }

    public Font getVersionFont() {
      return versionFont;
    }

    public Font getCopyrightFont() {
      return versionFont;
    }
  }

  private static class DefaultFonts extends Fonts {
    DefaultFonts() {
      nameFont = new Font("Lucida Grande", Font.BOLD, 14);
      versionFont = new Font("Lucida Grande", Font.PLAIN, 10);
    }
  }

  private static class GtkFonts extends Fonts {
    private static final float PANGO_SCALE_XX_LARGE = 1.2f * 1.2f * 1.2f;
    private static final float PANGO_SCALE_SMALL = 1 / 1.2f;

    GtkFonts() {
      Font font = UIManager.getFont("TextArea.font");
      nameFont = scale(font, PANGO_SCALE_XX_LARGE).deriveFont(Font.BOLD);
      versionFont = scale(font, PANGO_SCALE_SMALL);
    }

    private static Font scale(Font font, float factor) {
      return font.deriveFont(font.getSize2D() * factor);
    }
  }

  private static class WindowsFonts extends Fonts {
    WindowsFonts() {
      nameFont = versionFont = UIManager.getFont("MenuItem.font");
    }
  }
}
