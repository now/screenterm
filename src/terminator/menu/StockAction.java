package terminator.menu;

import java.awt.*;
import javax.swing.*;

import terminator.util.*;

abstract class StockAction extends AbstractAction {
  public StockAction(String name) {
    super(name);
  }

  public StockAction(String name, String stock) {
    super(name);
    stockify(stock);
  }

  private void stockify(String stock) {
    if (!OS.isGtk())
      return;
    Image image = (Image)Toolkit.getDefaultToolkit().
      getDesktopProperty(String.format("gtk.icon.%s.1.ltr", stock));
    if (image == null)
      return;
    putValue(Action.SMALL_ICON, new ImageIcon(image));
  }
}
