package terminator.menu;

import java.awt.*;
import java.awt.event.*;

abstract class FrameAction extends AcceleratableAction {
  private final Frame frame;

  public FrameAction(String name, Frame frame) {
    super(name);
    this.frame = frame;
  }

  public FrameAction(String name, String accelerator, Frame frame) {
    super(name, accelerator);
    this.frame = frame;
  }

  public void actionPerformed(ActionEvent e) {
    if (!isEnabled())
      return;
    frameActionPerformed(frame);
  }

  protected abstract void frameActionPerformed(Frame frame);

  @Override public boolean isEnabled() {
    return frame != null;
  }
}
