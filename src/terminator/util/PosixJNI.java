package terminator.util;

class PosixJNI {
  static {
    NativeLibrary.load("posix");
  }

  static native int get_EINTR();

  static native int get_SIGABRT();
  static native int get_SIGALRM();
  static native int get_SIGBUS();
  static native int get_SIGCHLD();
  static native int get_SIGCONT();
  static native int get_SIGFPE();
  static native int get_SIGHUP();
  static native int get_SIGILL();
  static native int get_SIGINT();
  static native int get_SIGKILL();
  static native int get_SIGPIPE();
  static native int get_SIGQUIT();
  static native int get_SIGSEGV();
  static native int get_SIGSTOP();
  static native int get_SIGTERM();
  static native int get_SIGTSTP();
  static native int get_SIGTTIN();
  static native int get_SIGTTOU();
  static native int get_SIGUSR1();
  static native int get_SIGUSR2();
  static native int get_SIGPROF();
  static native int get_SIGSYS();
  static native int get_SIGTRAP();
  static native int get_SIGURG();
  static native int get_SIGXCPU();
  static native int get_SIGXFSZ();

  static native int read(int fd, byte[] buffer, int offset, int count);
  static native int write(int fd, byte[] buffer, int offset, int count);
  static native int close(int fd);

  static native int waitpid(int pid, WaitStatus status, int flags);
  static native boolean wIfExited(int status);
  static native boolean wIfSignaled(int status);
  static native int wExitStatus(int status);
  static native int wTermSig(int status);
  static native boolean wCoreDump(int status);

  static native int killpg(int group, int signal);

  static native String strerror(int error);
}
