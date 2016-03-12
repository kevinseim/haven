package org.seim.haven.util;

import java.util.Arrays;

import org.seim.haven.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kevin Seim
 */
public class MemoryMonitor {

  private static final Logger log = LoggerFactory.getLogger(MemoryMonitor.class);
  
  private static final int SAMPLES = Settings.getLong("memcheck.samples", 60L).intValue();
  private static final long INTERVAL = Settings.getLong("memcheck.interval", 250L);
  private static final boolean LOG_ENABLED = Settings.getBoolean("memcheck.log", true);
  
  private int insert = 0;
  private long[] samples = new long[SAMPLES];
  
  public MemoryMonitor() {
    Arrays.fill(samples, 0);
  }
  
  public void start() {
    Thread t = new Thread() {
      public void run() {
        try {
          while (true) {
            sample();
            Thread.sleep(INTERVAL);
          }
        } catch (InterruptedException e) { }
      }
    };
    t.setName("memcheck");
    t.setDaemon(true);
    t.start();
  }
  
  public synchronized long[] getUsedRange() {
    long min = Long.MAX_VALUE;
    long max = Long.MIN_VALUE;
    for (long sample : samples) {
      if (sample != 0) {
        if (sample < min) {
          min = sample;
        }
        if (sample > max) {
          max = sample;
        }
      }
    }
    return new long[] { min, max };
  }
  
  private synchronized void sample() {
    Runtime rt = Runtime.getRuntime();
    samples[insert] = rt.totalMemory() - rt.freeMemory();
    if (++insert >= SAMPLES) {
      insert = 0;
      if (LOG_ENABLED && log.isInfoEnabled()) {
        long[] range = getUsedRange();
        log.info("Memory used: {} minimum, {} maximum", 
            ByteUtils.toHumanReadableByteCount(range[0]),
            ByteUtils.toHumanReadableByteCount(range[1]));
      }
    }
  }
}
