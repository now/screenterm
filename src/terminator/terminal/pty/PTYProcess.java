package terminator.terminal.pty;

import e.util.*;
import java.awt.Dimension;
import java.io.*;
import java.util.concurrent.*;
import org.jessies.os.*;

public class PTYProcess {
        private static final String CHARSET_NAME = "UTF-8";

        private static boolean libraryLoaded = false;

        private int fd = -1;
        private int pid;
        private String slavePtyName;

        private WaitStatus status;

        private InputStreamReader input;
        private OutputStreamWriter output;

        private final ExecutorService executorService = ThreadUtilities.newSingleThreadExecutor("Child Forker/Reaper");

        private static synchronized void ensureLibraryLoaded() throws UnsatisfiedLinkError {
                if (libraryLoaded)
                        return;
                FileUtilities.loadNativeLibrary("pty");
                libraryLoaded = true;
        }

        public PTYProcess(String executable, String[] argv, String workingDirectory) throws Exception {
                ensureLibraryLoaded();
                startProcess(executable, argv, workingDirectory);
                input = new InputStreamReader(new PTYInputStream(fd), CHARSET_NAME);
                output = new OutputStreamWriter(new PTYOutputStream(fd), CHARSET_NAME);
        }

        private void startProcess(final String executable, final String[] argv, final String workingDirectory) throws Exception {
                invoke(new Callable<Exception>() { public Exception call() {
                        try {
                                nativeStartProcess(executable, argv, workingDirectory);
                                return null;
                        } catch (Exception e) {
                                return e;
                        }
                }});
                if (pid == -1)
                        throw new IOException("Couldn’t start process \"" + executable + "\"");
        }

        private native void nativeStartProcess(String executable, String[] argv, String workingDirectory) throws IOException;

        public int read(char[] chars) throws IOException {
                return input.read(chars, 0, chars.length);
        }

        public void write(String string) throws IOException {
                if (status != null)
                        return;
                output.write(string);
                output.flush();
        }

        public native void sendResizeNotification(Dimension sizeInChars) throws IOException;

        public void waitFor() throws Exception {
                invoke(new Callable<Exception>() { public Exception call() {
                        try {
                                waitPID();
                                return null;
                        } catch (Exception e) {
                                return e;
                        }
                }});
                executorService.shutdownNow();
        }

        private void waitPID() throws IOException {
                Posix.close(fd);
                fd = -1;

                status = new WaitStatus();
                int result;
                do {
                        result = Posix.waitpid(pid, status, 0);
                } while (-result == Errno.EINTR);

                if (result < 0)
                        throw new IOException("Waiting for PTY process failed: " +
                                              Errno.toString(-result));

        }

        /**
        * Java 1.5.0_03 on Linux 2.4.27 doesn't seem to use LWP threads (according
        * to ps -eLf) for Java threads. Linux 2.4 is broken such that only the
        * Java thread which forked a child can wait for it.
        */
        private void invoke(Callable<Exception> callable) throws Exception {
                Exception exception = executorService.submit(callable).get();
                if (exception != null)
                        throw exception;
        }

        public String name() {
                return "Process " + pid + " (" + slavePtyName + ")";
        }

        public String toString() {
                return String.format("PtyProcess[pid=%d,fd=%d,pty=%s]",
                                     pid, fd, slavePtyName);
        }

        public String toExitString() {
                if (status == null)
                        return "Process is still running";
                else if (status.WIFEXITED())
                        return "Process exited with status " + status.WEXITSTATUS();
		else if (status.WIFSIGNALED())
                        return "Process killed by " +
                                Signal.toString(status.WTERMSIG()) +
                                (status.WCOREDUMP() ? " — core dumped" : "");
		else
                        return "Lost contact with process";
        }

        public void destroy() throws IOException {
                if (status != null)
                        return;
                int rc = Posix.killpg(pid, Signal.SIGHUP);
                if (rc < 0)
                        throw new IOException("Killing process failed: " +
                                              Errno.toString(-rc));
                status = new WaitStatus();
        }
}
