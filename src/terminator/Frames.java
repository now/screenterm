package terminator;

import java.awt.event.*;
import java.util.*;

public class Frames {
  private ArrayList<TerminatorFrame> list = new ArrayList<TerminatorFrame>();

  public TerminatorFrame add(final TerminatorFrame frame) {
    list.add(frame);

    frame.addWindowListener(new WindowAdapter() {
      @Override public void windowClosed(WindowEvent event) {
        list.remove(frame);
      }
    });

    return frame;
  }

  /* TODO: Move this to TerminatorFrame that could register the creation of
   * each frame? */
  public boolean isEmpty() {
    return list.isEmpty();
  }

  public boolean closeAll() {
    // We need to copy frames as we will be mutating it.
    for (TerminatorFrame frame : new ArrayList<TerminatorFrame>(list))
      frame.setVisible(false);
    return true;
  }
}
