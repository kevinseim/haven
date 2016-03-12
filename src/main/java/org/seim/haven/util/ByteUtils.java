package org.seim.haven.util;

public class ByteUtils {

  private ByteUtils() { }
  
  public static String toHumanReadableByteCount(long bytes) {
    int unit = 1000;
    if (bytes < unit) {
      return bytes + "b";
    }
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    return String.format("%.1f%sb", bytes / Math.pow(unit, exp),  "kmgtpe".charAt(exp-1));
  }
}
