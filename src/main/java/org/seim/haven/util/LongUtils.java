package org.seim.haven.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 
 * @author Kevin Seim
 *
 */
public class LongUtils {

  private LongUtils()  { }
  
  /**
   *
   */
  public static Long toLong(Object o) {
    Long result;
    if (o == null) {
      result = null;
    } else if (o instanceof Number) {
      Number n = (Number) o;
      result = n.longValue();
    } else {
      try {
        result = Long.valueOf(o.toString().trim());
      } catch (Exception e) {
        throw new NumberFormatException("Invalid long: " + o);
      }
    }
    return result;
  }

  /**
   * Safely gets the Long equivalent of a Long value (and throws exception if no
   * such conversion is possible).
   */
  public static Long toLong(Long l) {
    if (l == null) {
      return null;
    } else {
      return l.longValue();
    }
  }

  public static void main(String[] args) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    writeUnsignedLong(new DataOutputStream(out), 65535, 2);
    System.out.println(LongUtils.readUnsignedLong(new DataInputStream(new ByteArrayInputStream(out.toByteArray())), 2));
  }
    
  /**
   * 
   * @param value
   * @return
   */
  public static int getMinUnsignedBytes(long value) {
    if (value <= 0xFFL) { // 2 ^ 8
      return 1;
    } else if (value <= 0xFFFFL) {  // 2 ^ 16
      return 2;
    } else if (value <= 0xFFFF_FFL) { // 2 ^ 24
      return 3;
    } else if (value <= 0xFFFF_FFFFL) { // 2 ^ 32
      return 4;
    } else if (value <= 0xFFFF_FFFF_FFL) { // 2 ^ 40
      return 5;
    } else if (value <= 0xFFFF_FFFF_FFFFL) { // 2 ^ 48
      return 6;
    } else if (value <= 0xFFFF_FFFF_FFFF_FFL) { // 2 ^ 56
      return 7;
    } else {
      return 8;
    }
  }
    
  /**
   * 
   * @param out
   * @param value
   * @param n
   * @throws IOException
   */
  public static void writeUnsignedLong(DataOutputStream out, int n, long value) throws IOException {
    for (int i = (n - 1); i >= 0; i--) {
      out.write((byte) (value >> (i * 8)));
    }
  }
    
  /**
   * Reads a long value from an input stream using the specified number
   * of bytes.  Bytes are assumed to be unsigned and big endian.
   * @param in the {@link DataInputStream} to read the value from
   * @param n number of bytes used to encode the Long
   * @return the long read from the input stream
   */
  public static long readUnsignedLong(DataInputStream in, int n) throws IOException {
    switch (n) {
    case 1:
      return ((in.readByte() & 0xffL) << 0);
    case 2:
      return ((in.readByte() & 0xffL) << 8) | 
             ((in.readByte() & 0xffL) << 0);
    case 3:
      return ((in.readByte() & 0xffL) << 16) | 
             ((in.readByte() & 0xffL) << 8) |
             ((in.readByte() & 0xffL) << 0);
    case 4:
      return ((in.readByte() & 0xffL) << 24) | 
             ((in.readByte() & 0xffL) << 16) |
             ((in.readByte() & 0xffL) << 8) |
             ((in.readByte() & 0xffL) << 0);
    case 5:
      return ((in.readByte() & 0xffL) << 32) | 
             ((in.readByte() & 0xffL) << 24) |
             ((in.readByte() & 0xffL) << 16) |
             ((in.readByte() & 0xffL) << 8) |
             ((in.readByte() & 0xffL) << 0);
    case 6:
      return ((in.readByte() & 0xffL) << 40) | 
             ((in.readByte() & 0xffL) << 32) |
             ((in.readByte() & 0xffL) << 24) |
             ((in.readByte() & 0xffL) << 16) |
             ((in.readByte() & 0xffL) << 8) |
             ((in.readByte() & 0xffL) << 0);
    case 7:
      return ((in.readByte() & 0xffL) << 48) | 
             ((in.readByte() & 0xffL) << 40) |
             ((in.readByte() & 0xffL) << 32) |
             ((in.readByte() & 0xffL) << 24) |
             ((in.readByte() & 0xffL) << 16) |
             ((in.readByte() & 0xffL) << 8) |
             ((in.readByte() & 0xffL) << 0);
    case 8:
      return in.readLong();
        
    default:
      throw new IllegalArgumentException("Invalid precision: " + n);
    }
  }
}
