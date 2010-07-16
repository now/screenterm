#ifdef __CYGWIN__
#  include <windows.h>
#endif

#include "terminator_util_PosixJNI.h"
#include "unix_exception.h"

#include <errno.h>
#include <signal.h>
#include <string.h>
#include <sys/wait.h>
#include <unistd.h>
#include <vector>

jint terminator_util_PosixJNI::get_1EINTR() { return EINTR; }

jint terminator_util_PosixJNI::get_1SIGABRT() { return SIGABRT; }
jint terminator_util_PosixJNI::get_1SIGALRM() { return SIGALRM; }
jint terminator_util_PosixJNI::get_1SIGBUS() { return SIGBUS; }
jint terminator_util_PosixJNI::get_1SIGCHLD() { return SIGCHLD; }
jint terminator_util_PosixJNI::get_1SIGCONT() { return SIGCONT; }
jint terminator_util_PosixJNI::get_1SIGFPE() { return SIGFPE; }
jint terminator_util_PosixJNI::get_1SIGHUP() { return SIGHUP; }
jint terminator_util_PosixJNI::get_1SIGILL() { return SIGILL; }
jint terminator_util_PosixJNI::get_1SIGINT() { return SIGINT; }
jint terminator_util_PosixJNI::get_1SIGKILL() { return SIGKILL; }
jint terminator_util_PosixJNI::get_1SIGPIPE() { return SIGPIPE; }
jint terminator_util_PosixJNI::get_1SIGQUIT() { return SIGQUIT; }
jint terminator_util_PosixJNI::get_1SIGSEGV() { return SIGSEGV; }
jint terminator_util_PosixJNI::get_1SIGSTOP() { return SIGSTOP; }
jint terminator_util_PosixJNI::get_1SIGTERM() { return SIGTERM; }
jint terminator_util_PosixJNI::get_1SIGTSTP() { return SIGTSTP; }
jint terminator_util_PosixJNI::get_1SIGTTIN() { return SIGTTIN; }
jint terminator_util_PosixJNI::get_1SIGTTOU() { return SIGTTOU; }
jint terminator_util_PosixJNI::get_1SIGUSR1() { return SIGUSR1; }
jint terminator_util_PosixJNI::get_1SIGUSR2() { return SIGUSR2; }
jint terminator_util_PosixJNI::get_1SIGPROF() { return SIGPROF; }
jint terminator_util_PosixJNI::get_1SIGSYS() { return SIGSYS; }
jint terminator_util_PosixJNI::get_1SIGTRAP() { return SIGTRAP; }
jint terminator_util_PosixJNI::get_1SIGURG() { return SIGURG; }
jint terminator_util_PosixJNI::get_1SIGXCPU() { return SIGXCPU; }
jint terminator_util_PosixJNI::get_1SIGXFSZ() { return SIGXFSZ; }

static jint result_or_minus_errno(int result) {
        return result == -1 ? -errno : result;
}

static jint zero_or_minus_errno(int result) {
        return result == -1 ? -errno : 0;
}

jint terminator_util_PosixJNI::read(jint fd, jbyteArray buffer, jint offset, jint count) {
        if (count == 0)
                return 0;

        std::vector<jbyte> native(count);
        ssize_t n = ::read(fd, &native[0], count);
        if (n > 0)
                m_env->SetByteArrayRegion(buffer, offset, n, &native[0]);

        return result_or_minus_errno(n);
}

jint terminator_util_PosixJNI::write(jint fd, jbyteArray buffer, jint offset, jint count) {
        if (count == 0)
                return 0;

        std::vector<jbyte> native(count);
        m_env->GetByteArrayRegion(buffer, offset, count, &native[0]);
        if (m_env->ExceptionCheck())
                return -1;

        return result_or_minus_errno(::write(fd, &native[0], count));
}

jint terminator_util_PosixJNI::close(jint fd) {
        return zero_or_minus_errno(::close(fd));
}

jint terminator_util_PosixJNI::waitpid(jint pid, jobject javaWaitStatus, jint flags) {
        int status = 0;
        pid_t result = ::waitpid(pid, &status, flags);
        if (result == -1)
                return -errno;
        JniField<jint, false> field(m_env, javaWaitStatus, "status", "I");
        field = status;

        return result;
}

jboolean terminator_util_PosixJNI::wIfExited(jint status) {
        return WIFEXITED(status);
}

jboolean terminator_util_PosixJNI::wIfSignaled(jint status) {
        return WIFSIGNALED(status);
}

jint terminator_util_PosixJNI::wExitStatus(jint status) {
        return WEXITSTATUS(status);
}

jint terminator_util_PosixJNI::wTermSig(jint status) {
        return WTERMSIG(status);
}

jboolean terminator_util_PosixJNI::wCoreDump(jint status) {
#ifdef WCOREDUMP
        return WCOREDUMP(status) ? JNI_TRUE : JNI_FALSE;
#else
        return JNI_FALSE;
#endif
}

jint terminator_util_PosixJNI::killpg(jint group, jint signal) {
        return zero_or_minus_errno(::killpg(group, signal));
}

jstring terminator_util_PosixJNI::strerror(jint error) {
        return m_env->NewStringUTF(unix_exception::to_string(error).c_str());
}
