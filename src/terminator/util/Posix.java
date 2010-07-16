package terminator.util;

public class Posix {
  public static int read(int fd, byte[] buffer, int offset, int count) {
    checkBufferArgs(buffer, offset, count);
    return PosixJNI.read(fd, buffer, offset, count);
  }

  public static int write(int fd, byte[] buffer, int offset, int count) {
    checkBufferArgs(buffer, offset, count);
    return PosixJNI.write(fd, buffer, offset, count);
  }

  public static int close(int fd) {
    return PosixJNI.close(fd);
  }

  public static int waitpid(int pid, WaitStatus status, int flags) {
    return PosixJNI.waitpid(pid, status, flags);
  }

  public static int killpg(int group, int signal) {
    return PosixJNI.killpg(group, signal);
  }

  private static void checkBufferArgs(byte[] buffer, int offset, int count) {
    if (buffer == null)
      throw new NullPointerException("buffer must not be null");
    if (offset < 0 || offset > buffer.length)
      throw new IllegalArgumentException(String.format("offset must be between 0 and %d (%d)",
                                                       buffer.length, offset));
    if (count < 0 || count > buffer.length - offset)
      throw new IllegalArgumentException(String.format("count must be between 0 and (%d - %d) (%d)",
                                         buffer.length, offset, count));
  }
}
