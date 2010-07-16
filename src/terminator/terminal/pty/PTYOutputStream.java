package terminator.terminal.pty;

import java.io.*;

import terminator.util.*;

class PTYOutputStream extends OutputStream {
        private int fd;

        public PTYOutputStream(int fd) {
                this.fd = fd;
        }

        @Override
        public void write(int b) throws IOException {
                throw new UnsupportedOperationException();
        }

        @Override
        public void write(byte[] bytes, int offset, int count) throws IOException {
                while (count > 0) {
                        int n = Posix.write(fd, bytes, offset, count);
                        if (n < 0 && n == -Errno.EINTR)
                                continue;
                        if (n < 0)
                                throw new IOException("Writing to PTY failed: " +
                                                      new Errno(-n).toString());
                        offset += n;
                        count -= n;
                }
        }
}
