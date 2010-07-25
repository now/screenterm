package terminator.menu;

import javax.swing.*;

abstract class AcceleratableAction extends AbstractAction {
  public AcceleratableAction(String name) {
    super(name);
  }

  public AcceleratableAction(String name, String accelerator) {
    this(name, MenuBar.makeKeyStroke(accelerator));
  }

  public AcceleratableAction(String name, KeyStroke accelerator) {
    super(name);
    putValue(ACCELERATOR_KEY, accelerator);
  }
}
