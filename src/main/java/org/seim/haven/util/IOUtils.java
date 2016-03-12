package org.seim.haven.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Kevin Seim
 */
public class IOUtils {

  private IOUtils() { }

  public static void closeQuietly(Closeable c) {
    try {
      if (c != null) {
        c.close();
      }
    } catch (IOException e) { }
  }
}
