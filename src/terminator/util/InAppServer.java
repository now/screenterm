package terminator.util;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.security.*;
import java.util.*;

public final class InAppServer<T> {
  private final String name;
  private final T handler;
  private Secret secret;

  public InAppServer(String name, String port, InetAddress address, T handler) {
    this.name = name + "Server";
    this.handler = handler;

    if (isRunningAsRoot())
      return;

    start(port, address);
  }

  private boolean isRunningAsRoot() {
    if (!System.getProperty("user.name").equals("root"))
      return false;
    Log.warn("InAppServer: refusing to start unauthenticated server “%s” as root.",
             name);
    return true;
  }

  private void start(String port, InetAddress address) {
    try {
      File portFile = new File(port);
      Thread listener = new Thread(new Listener(portFile, address), name);
      listener.setDaemon(true);
      listener.start();
      secret = new Secret(portFile);
    } catch (Throwable t) {
      Log.warn(t, "InAppServer: couldn’t start “%s”", name);
    }
  }

  private void process(String line, PrintWriter out) {
    try {
      invoke(line, out, line.split("[\t ]"));
    } catch (NoSuchMethodException e) {
      out.format("%s: unknown request: %s\n", name, line);
    } catch (Exception e) {
      Log.warn(e, "%s: exception thrown while handling command: %s", name, line);
      out.format("%s: request denied: %s: %s\n", name, line, e.toString());
    }
  }

  private void invoke(String line, PrintWriter out, String[] fields) throws Exception {
    for (Method m : handler.getClass().getMethods())
      if (m.getName().equals(fields[0]) && m.getReturnType() == void.class) {
        invoke(m, line, out, fields);
        return;
      }
    throw new NoSuchMethodException();
  }

  private void invoke(Method method, String line, PrintWriter out, String[] fields) throws Exception {
    if (!invoke(method, line, out))
      invoke(method, out, fields);
  }

  private boolean invoke(Method method, String line, PrintWriter out) throws Exception {
    Class<?>[] types = method.getParameterTypes();
    if (types.length != 2 || types[0] != PrintWriter.class || types[1] != String.class)
      return false;
    invoke(method, new Object[]{ out, line });
    return true;
  }

  private void invoke(Method method, PrintWriter out, String[] fields) throws Exception {
    int i = 1;
    List<Object> arguments = new ArrayList<Object>();
    for (Class<?> type : method.getParameterTypes())
      if (type == PrintWriter.class)
        arguments.add(out);
      else if (type == String.class)
        arguments.add(fields[i++]);
    invoke(method, arguments.toArray());
  }

  private void invoke(Method method, Object[] arguments) throws Exception {
    method.invoke(handler, arguments);
  }

  private static void writeFile(File file, String contents) throws IOException {
    PrintWriter out =
      new PrintWriter(new OutputStreamWriter(new FileOutputStream(file),
                                             "UTF-8"));
    try {
      out.print(contents);
    } finally {
      out.close();
    }
  }

  private static class Secret {
    private static final SecureRandom generator = new SecureRandom();
    private final File file;
    private final String secret;

    Secret(Secret secret) throws IOException {
      this(secret.file);
    }

    Secret(File port) throws IOException {
      file = new File(port.getPath() + ".secret");
      secret = Long.toString(generator.nextLong());
      writeFile(file, secret);
    }

    boolean equals(String value) {
      return secret.equals(value);
    }
  }

  private class Listener implements Runnable {
    private final ServerSocket socket = new ServerSocket();

    Listener(File port, InetAddress address) throws IOException {
      socket.bind(new InetSocketAddress(address, 0));
      writeFile(port, String.format("%s:%d\n",
                                    lookup(address).getHostName(),
                                    socket.getLocalPort()));
    }

    private InetAddress lookup(InetAddress address) {
      if (address != null)
        return address;
      try {
        return InetAddress.getLocalHost();
      } catch (UnknownHostException e) {
        Log.warn("Problem finding a local IP address.", e);
        return socket.getInetAddress();
      }
    }

    public void run() {
      while (true)
        accept();
    }

    void accept() {
      try {
        new Thread(new Client(socket.accept()),
                   String.format("%s-Handler-%d",
                                 name, Thread.activeCount())).start();
      } catch (Exception e) {
        Log.warn(e, "%s: exception accepting connection.", name);
      }
    }
  }

  private class Client implements Runnable {
    private final Socket socket;

    private Client(Socket socket) {
      this.socket = socket;
    }

    public void run() {
      try {
        handle();
      } catch (Exception e) {
        warn(e, "failed to handle client request");
      } finally {
        close();
      }
    }

    private void handle() throws IOException {
      BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
      try {
        if (authenticate(in.readLine(), out))
          handleRequest(in.readLine(), out);
      } finally {
        out.close();
        in.close();
      }
    }

    private boolean authenticate(String line, PrintWriter out) throws IOException {
      if (!secret.equals(line)) {
        warn("failed authentication attempt with “%s”", line);
        out.println("Authentication failed");
        return false;
      }
      secret = new Secret(secret);
      out.println("Authentication OK");
      return true;
    }

    private void handleRequest(String line, PrintWriter out) throws IOException {
      if (line == null || line.isEmpty()) {
        warn("ignoring empty request");
        return;
      }
      process(line, out);
    }

    private void close() {
      try {
        socket.close();
      } catch (IOException e) {
        warn(e, "failed to close client socket");
      }
    }

    private void warn(String format, Object... args) {
      warn(null, format, args);
    }

    private void warn(Exception e, String format, Object... args) {
      Log.warn(e, "%s: %s.",
               Thread.currentThread().getName(), String.format(format, args));
    }
  }
}
