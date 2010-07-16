package terminator.util;

public class Errno {
  public static final int EINTR = PosixJNI.get_EINTR();

  private int error;

  public Errno(int error) {
    this.error = error;
  }

  public String toString() {
    return PosixJNI.strerror(error);
  }
}
