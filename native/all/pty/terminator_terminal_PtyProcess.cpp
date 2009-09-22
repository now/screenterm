#ifdef __CYGWIN__
#include <windows.h>
#endif

#include "terminator_terminal_PtyProcess.h"

#include "DirectoryIterator.h"
#include "JniString.h"
#include "join.h"
#include "PtyGenerator.h"
#include "toString.h"
#include "unix_exception.h"

#include <stdlib.h>
#include <sys/stat.h>
#ifdef __APPLE__ // sysctl.h doesn't exist on Cygwin.
#include <sys/sysctl.h>
#endif
#include <sys/types.h>
#include <termios.h>

// Deque is the default choice of container in C++.
// Using vector connotes a requirement for contiguity.
// See http://www.gotw.ca/gotw/054.htm.
#include <deque>
#include <fstream>
#include <string>
#include <vector>

typedef std::vector<std::string> StringArray;

struct JavaStringArrayToStringArray : StringArray {
    JavaStringArrayToStringArray(JNIEnv* env, jobjectArray javaStringArray) {
        int arrayLength = env->GetArrayLength(javaStringArray);
        for (int i = 0; i != arrayLength; ++i) {
            jstring s = static_cast<jstring>(env->GetObjectArrayElement(javaStringArray, i));
            push_back(JniString(env, s));
        }
    }
};

struct Argv : std::vector<char*> {
    // Non-const because execvp is anti-social about const.
    Argv(StringArray& arguments) {
        for (StringArray::iterator it = arguments.begin(); it != arguments.end(); ++it) {
            // We must point to the memory in arguments, not a local.
            std::string& argument = *it;
            push_back(&argument[0]);
        }
        // execvp wants a null-terminated array of pointers to null terminated strings.
        push_back(0);
    }
};

static void waitUntilFdWritable(int fd) {
    int rc;
    do {
        fd_set fds;
        FD_ZERO(&fds);
        FD_SET(fd, &fds);
        rc = ::select(fd + 1, 0, &fds, 0, 0);
    } while (rc == -1 && errno == EINTR);
    if (rc != 1) {
        throw unix_exception("select(" + toString(fd) + ", ...) failed");
    }
}

void terminator_terminal_PtyProcess::nativeStartProcess(jstring javaExecutable, jobjectArray javaArgv, jstring javaWorkingDirectory) {
    PtyGenerator ptyGenerator;
    fd = ptyGenerator.openMaster();
    
    JavaStringArrayToStringArray arguments(m_env, javaArgv);
    Argv argv(arguments);
    std::string executable(JniString(m_env, javaExecutable));
    
    std::string workingDirectory("");
    if (javaWorkingDirectory != 0) {
        workingDirectory = JniString(m_env, javaWorkingDirectory);
    }
    
    pid = ptyGenerator.forkAndExec("terminator", executable, &argv[0], workingDirectory);
    
    // On Linux, the TIOCSWINSZ ioctl sets the size of the pty (without blocking) even if it hasn't been opened by the child yet.
    // On Mac OS, it silently does nothing, meaning that when the child does open the pty, TIOCGWINSZ reports the wrong size.
    // We work around this by explicitly blocking the parent until the child has opened the pty.
    // We can recognize this on Mac OS by the fact that a write would no longer block.
    // (The fd is writable on Linux even before the child has opened the pty.)
    // FIXME: If the fd never becomes writable, for example because the specified working directory doesn't exist, then this locks up the whole Terminator.
    waitUntilFdWritable(fd.get());
    
    slavePtyName = newStringUtf8(ptyGenerator.getSlavePtyName());
}

void
terminator_terminal_PtyProcess::sendResizeNotification(jobject jSize)
{
        if (fd.get() == -1)
                return;

        struct winsize size;
        size.ws_col = JniField<jint, false>(m_env, jSize, "width", "I").get();
        size.ws_row = JniField<jint, false>(m_env, jSize, "height", "I").get();
        if (ioctl(fd.get(), TIOCSWINSZ, &size) < 0)
                throw unix_exception("I/O control command TIOCSWINSZ failed on " +
                                     toString(fd.get()));
}
