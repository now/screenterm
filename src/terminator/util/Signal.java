package terminator.util;

import java.util.*;

public class Signal {
  public static final int SIGABRT = PosixJNI.get_SIGABRT();
  public static final int SIGALRM = PosixJNI.get_SIGALRM();
  public static final int SIGBUS = PosixJNI.get_SIGBUS();
  public static final int SIGCHLD = PosixJNI.get_SIGCHLD();
  public static final int SIGCONT = PosixJNI.get_SIGCONT();
  public static final int SIGFPE = PosixJNI.get_SIGFPE();
  public static final int SIGHUP = PosixJNI.get_SIGHUP();
  public static final int SIGILL = PosixJNI.get_SIGILL();
  public static final int SIGINT = PosixJNI.get_SIGINT();
  public static final int SIGKILL = PosixJNI.get_SIGKILL();
  public static final int SIGPIPE = PosixJNI.get_SIGPIPE();
  public static final int SIGQUIT = PosixJNI.get_SIGQUIT();
  public static final int SIGSEGV = PosixJNI.get_SIGSEGV();
  public static final int SIGSTOP = PosixJNI.get_SIGSTOP();
  public static final int SIGTERM = PosixJNI.get_SIGTERM();
  public static final int SIGTSTP = PosixJNI.get_SIGTSTP();
  public static final int SIGTTIN = PosixJNI.get_SIGTTIN();
  public static final int SIGTTOU = PosixJNI.get_SIGTTOU();
  public static final int SIGUSR1 = PosixJNI.get_SIGUSR1();
  public static final int SIGUSR2 = PosixJNI.get_SIGUSR2();
  public static final int SIGPROF = PosixJNI.get_SIGPROF();
  public static final int SIGSYS = PosixJNI.get_SIGSYS();
  public static final int SIGTRAP = PosixJNI.get_SIGTRAP();
  public static final int SIGURG = PosixJNI.get_SIGURG();
  public static final int SIGXCPU = PosixJNI.get_SIGXCPU();
  public static final int SIGXFSZ = PosixJNI.get_SIGXFSZ();

  private static Map<Integer, String> names = new HashMap<Integer, String>();
  static {
    names.put(SIGABRT, "SIGABRT");
    names.put(SIGALRM, "SIGALRM");
    names.put(SIGBUS, "SIGBUS");
    names.put(SIGCHLD, "SIGCHLD");
    names.put(SIGCONT, "SIGCONT");
    names.put(SIGFPE, "SIGFPE");
    names.put(SIGHUP, "SIGHUP");
    names.put(SIGILL, "SIGILL");
    names.put(SIGINT, "SIGINT");
    names.put(SIGKILL, "SIGKILL");
    names.put(SIGPIPE, "SIGPIPE");
    names.put(SIGQUIT, "SIGQUIT");
    names.put(SIGSEGV, "SIGSEGV");
    names.put(SIGSTOP, "SIGSTOP");
    names.put(SIGTERM, "SIGTERM");
    names.put(SIGTSTP, "SIGTSTP");
    names.put(SIGTTIN, "SIGTTIN");
    names.put(SIGTTOU, "SIGTTOU");
    names.put(SIGUSR1, "SIGUSR1");
    names.put(SIGUSR2, "SIGUSR2");
    names.put(SIGPROF, "SIGPROF");
    names.put(SIGSYS, "SIGSYS");
    names.put(SIGTRAP, "SIGTRAP");
    names.put(SIGURG, "SIGURG");
    names.put(SIGXCPU, "SIGXCPU");
    names.put(SIGXFSZ, "SIGXFSZ");
  };

  private int signal;

  public Signal(int signal) {
    this.signal = signal;
  }

  public String toString(int signal) {
    if (!names.containsKey(signal))
      return String.format("signal %d", signal);
    return String.format("signal %d (%s)", signal, names.get(signal));
  }
}
