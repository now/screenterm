package terminator.util;

import java.io.*;

public class NativeLibrary {
  private static final String LIBS_SYSTEM_PROPERTY = "org.jessies.libraryDirectories";

  public static void load(String name) {
    final String directories = System.getProperty(LIBS_SYSTEM_PROPERTY);
    if (directories == null)
      loadLibrary(name);

    Throwable cause = null;
    final String library = System.mapLibraryName(name);
    for (String directory : directories.split(File.pathSeparator)) {
      cause = tryLoad(new File(directory, library), cause);
      if (cause == null)
        return;
    }

    throwError(library, directories, cause);
  }

  private static void loadLibrary(String name) {
    try {
      System.loadLibrary(name);
    } catch (UnsatisfiedLinkError cause) {
      throwError(name, null, cause);
    }
  }

  private static Throwable tryLoad(File path, Throwable cause) {
    try {
      System.load(path.getAbsolutePath());
      return null;
    } catch (Throwable t) {
      if (cause != null)
        t.initCause(cause);
      return t;
    }
  }

  private static void throwError(String library, String directories, Throwable cause) {
    UnsatisfiedLinkError unsatisfiedLinkError =
      new UnsatisfiedLinkError(String.format("Failed to load “%s” for %s %s",
                                             library,
                                             System.getProperty("os.arch"),
                                             (directories != null) ?
                                             String.format("from “%s”",
                                                           directories) :
                                             String.format("(%s was not set)",
                                                           LIBS_SYSTEM_PROPERTY)));
    unsatisfiedLinkError.initCause(cause);
    throw unsatisfiedLinkError;
  }

  private NativeLibrary() {
  }
}
