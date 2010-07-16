package terminator.terminal.pty;

import java.io.*;

import terminator.util.*;

class PTYInputStream extends InputStream {
  private int fd;

  public PTYInputStream(int fd) {
    this.fd = fd;
  }

  @Override
    public int read() throws IOException {
      throw new UnsupportedOperationException();
    }

  @Override
    public int read(byte[] bytes, int offset, int count) throws IOException {
      int n;
      do {
        n = Posix.read(fd, bytes, offset, count);
      } while (n < 0 && -n == Errno.EINTR);
      if (n >= 0)
        return n;
      throw new IOException("Reading from PTY failed: " + new Errno(-n).toString());
    }
}
