package org.seim.haven.util;

import java.nio.charset.Charset;
import java.util.Arrays;

public class Charsets {
  
  public static final Charset ASCII = Charset.forName("ASCII");
  public static final Charset UTF8 = Charset.forName("UTF-8");
  
  public static byte[] getBytes(long n) {
    String s = Long.toString(n);
    int length = s.length();
    byte[] value = new byte[length];
    for (int i=0; i<length; i++) {
      value[i] = (byte) s.charAt(i);
    }
    return value;
  }
  
  public static long toLong(byte[] value) {
    StringBuilder sb = new StringBuilder();
    for (byte b : value) {
      sb.append((char)b);
    }
    return Long.valueOf(sb.toString());
  }
  
  public static void main(String[] args) {
    System.out.println(Arrays.toString(getBytes(59)));
    System.out.println(toLong(getBytes(59)));
  }
  
}
