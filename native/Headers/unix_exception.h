#ifndef UNIX_EXCEPTION_H
#define UNIX_EXCEPTION_H

#include <errno.h>
#include <stdexcept>
#include <sstream>
#include <string.h>
#include <string>

/* NOTE: This is not re-entrant, but it doesn’t matter as, on MINGW, we don’t
 * have any multi-threaded utilities. */
#ifdef __MINGW32__
int strerror_r(int error, char *buffer, size_t size) {
        const char *str = strerror(error);
        strncpy(buffer, str, size);
        buffer[size - 1] = '\0';
        return 0;
}
#endif

class unix_exception : public std::runtime_error {
public:
        unix_exception(const std::string& message)
        : std::runtime_error(message + (errno ? ": (" + to_string(errno) + ")" : " (but errno is empty)"))
        , m_errno(errno) {
        }

        static std::string to_string(int error) {
                std::ostringstream os;
                char buffer[1024];
                int result = strerror_r(error, buffer, sizeof(buffer));

                switch (result) {
                case EINVAL: /* Unknown error messages are displayed as such anyway. */
                case ERANGE: /* Truncated error messages are fine. */
                case 0:
                        return buffer;
                default:
                        os << "Decoding error " << error << " produced " << result;
                        return os.str();
                }
        }

private:
        const int m_errno;
};

#endif
