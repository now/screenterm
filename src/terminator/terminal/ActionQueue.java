package terminator.terminal;

import java.awt.*;
import java.util.*;
import java.util.concurrent.*;

import terminator.model.*;
import terminator.util.*;

public class ActionQueue {
        private ArrayList<TerminalAction> actions = new ArrayList<TerminalAction>();
        private Semaphore eqFlowControl = new Semaphore(30);
        private TerminalModel model;
        
        public ActionQueue(TerminalModel model) {
                this.model = model;
        }

        public synchronized void add(TerminalAction action) {
                actions.add(action);
        }

        public synchronized void flush() {
                if (actions.size() == 0)
                        return;

                final TerminalAction[] as = actions.toArray(new TerminalAction[actions.size()]);
                actions.clear();

                try {
                        eqFlowControl.acquire();
                } catch (InterruptedException e) {
                        Log.warn("Interrupted while trying to flush terminal actions", e);
                        return;
                }

                EventQueue.invokeLater(new Runnable() {
                        public void run() {
                                try {
                                        model.processActions(as);
                                } catch (Throwable t) {
                                        Log.warn("Couldn't process terminal actions", t);
                                } finally {
                                        eqFlowControl.release();
                                }
                        }
                });
        }
}
