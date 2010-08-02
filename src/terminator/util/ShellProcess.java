package terminator.util;

class ShellProcess {
  private String[] args;

  public static Process spawn(String... args) {
    return new ShellProcess(args).spawn();
  }

  public ShellProcess(String... args) {
    this.args = args;
  }

  public Process spawn() {
    Process result = null;
    try {
      final Process p = Runtime.getRuntime().exec(args);
      result = p;
      new Thread("Shell Process: " + args.toString()) {
        public void run() {
          try {
            p.getInputStream().close();
            p.getOutputStream().close();
            p.getErrorStream().close();
            p.waitFor();
          } catch (Exception e) {
            Log.warn(String.format("Problem waiting for %s to finish.",
                                   args.toString()), e);
          }
        }
      }.start();
    } catch (Exception e) {
      Log.warn(String.format("Problem starting %s.", args.toString()), e);
    }
    return result;
  }
}
