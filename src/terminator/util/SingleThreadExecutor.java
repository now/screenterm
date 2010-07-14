package terminator.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class SingleThreadExecutor {
  public static ExecutorService create(final String threadName) {
    return Executors.newSingleThreadExecutor(new DaemonThreadFactory() {
      public String newThreadName() {
        return threadName;
      }
    });
  }

  private static abstract class DaemonThreadFactory implements ThreadFactory {
    public abstract String newThreadName();

    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r, newThreadName());
      thread.setDaemon(true);
      thread.setPriority(Thread.NORM_PRIORITY);
      return thread;
    }
  }

  private SingleThreadExecutor() {
  }
}
