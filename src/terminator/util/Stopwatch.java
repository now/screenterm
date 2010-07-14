package terminator.util;

import java.util.*;

public class Stopwatch {
  private static final Map<String, Stopwatch> stopwatches = new HashMap<String, Stopwatch>();

  private String name;
  private long samples = 0;
  private long min = Long.MAX_VALUE;
  private long max = 0;
  private long sum = 0;

  private Stopwatch(String name) {
    this.name = name;
  }

  public static Stopwatch get(String name) {
    synchronized (stopwatches) {
      Stopwatch stopwatch = stopwatches.get(name);
      if (stopwatch != null)
        return stopwatch;
      Stopwatch fresh = new Stopwatch(name);
      stopwatches.put(name, fresh);
      return fresh;
    }
  }

  public Timer start() {
    return new Timer();
  }

  @Override public String toString() {
    return String.format("%s: %s",
                         name,
                         samples == 0 ?
                         "(no samples)" :
                         String.format("%d, %s total, %d..%d (mean %d)",
                                       samples,
                                       samples == 1 ? "sample" : "samples",
                                       nsToString(sum),
                                       nsToString(min),
                                       nsToString(max),
                                       nsToString(sum / samples)));
  }

  public static String toStringAll() {
    Stopwatch[] all;
    synchronized (stopwatches) {
      all = stopwatches.values().toArray(new Stopwatch[stopwatches.size()]);
    }

    StringBuilder result = new StringBuilder();
    for (Stopwatch stopwatch : all) {
      result.append(stopwatch.toString());
      result.append("\n");
    }
    if (all.length == 0)
      result.append("(no stopwatches)");
    return result.toString();
  }

  private static String nsToString(final long ns) {
    StringBuilder result = new StringBuilder();
    appendDuration(result, ns / 86400000000000L, 'd');
    appendDuration(result, ns / 3600000000000L % 24, 'h');
    appendDuration(result, ns / 60000000000L % 60, 'm');
    appendSeconds(result, ns);
    appendSmall(result, ns, 1000000000L, "ms");
    appendSmall(result, ns, 1000000L, "us");
    appendSmall(result, ns, 1000L, "ns");
    return result.toString();
  }

  private static void appendDuration(StringBuilder result, long duration, char unit) {
    if (duration == 0 && result.length() == 0)
      return;
    result.append(duration);
    result.append(unit);
  }

  private static void appendSeconds(StringBuilder result, long ns) {
    if (result.length() == 0)
      result.append(String.format("%.2fs", ns / 1000000000.0));
    else
      appendDuration(result, ns / 1000000000L % 60, 's');
  }

  private static void appendSmall(StringBuilder result, long ns, long limit, String unit) {
    if (result.length() > 0 || ns >= limit)
      return;
    result.append(ns / (limit / 1000L));
    result.append(unit);
  }

  private synchronized void record(long ns) {
    samples++;
    if (min > ns)
      min = ns;
    if (max < ns)
      max = ns;
    sum += ns;
  }

  public class Timer {
    final long start = System.nanoTime();
    long stop;

    private Timer() {
    }

    public void stop() {
      stop = System.nanoTime();
      record(ns());
    }

    public long ns() {
      return stop - start;
    }
  }
}
