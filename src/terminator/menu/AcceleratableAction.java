package terminator.menu;

import javax.swing.*;

abstract class AcceleratableAction extends StockAction {
  public AcceleratableAction(String name) {
    super(name);
  }

  public AcceleratableAction(String name, String accelerator) {
    this(name, MenuBar.makeKeyStroke(accelerator));
  }

  public AcceleratableAction(String name, String accelerator, String stock) {
    this(name, MenuBar.makeKeyStroke(accelerator), stock);
  }

  public AcceleratableAction(String name, KeyStroke accelerator) {
    super(name);
    putValue(ACCELERATOR_KEY, accelerator);
  }

  public AcceleratableAction(String name, KeyStroke accelerator, String stock) {
    super(name, stock);
    putValue(ACCELERATOR_KEY, accelerator);
  }
}
