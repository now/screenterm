package terminator.util;

public class WaitStatus {
  private int status;

  public boolean exited() {
    return PosixJNI.wIfExited(status);
  }

  public boolean signaled() {
    return PosixJNI.wIfSignaled(status);
  }

  public int exitStatus() {
    return PosixJNI.wExitStatus(status);
  }

  public int terminationSignal() {
    return PosixJNI.wTermSig(status);
  }

  public boolean coreDumped() {
    return PosixJNI.wCoreDump(status);
  }
}
