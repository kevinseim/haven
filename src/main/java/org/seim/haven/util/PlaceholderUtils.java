package org.seim.haven.util;

import java.util.Properties;

/**
 * @author Kevin Seim
 */
public class PlaceholderUtils {

  private PlaceholderUtils() { }
  
  public static String resolve(String text, Properties props) {
    StringBuilder b = null;
    
    int from = -1;
    int state = 0;
    for (int i=0; i<text.length(); i++) {
      
      char c = text.charAt(i);
      switch (state) {
      case 0:
        if (c == '$') {
          state = 1;
        } else if (b != null) {
          b.append(c);
        }
        break;
        
      case 1:
        if (c == '{') {
          from = i + 1;
          state = 2;
        }
        else {
          if (b != null) {
            b.append('$').append(c);
          }
          state = 0;
        }
        break;
        
      case 2:
        if (c == '}') {
          String key = text.substring(from, i);
          String value = System.getProperty(key);
          if (value == null) {
            value = props.getProperty(key);
          }
          if (value != null) {
            if (b == null) {
              b = new StringBuilder();
            }
            b.append(value);
          }
          else if (b != null) {
            b.append("${").append(key).append("}");
          }
          state = 0;
        }
        break;
      }
    }
    
    return b != null ? b.toString() : text;
  }
}
