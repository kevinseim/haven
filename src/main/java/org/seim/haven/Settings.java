package org.seim.haven;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.seim.haven.util.IOUtils;
import org.seim.haven.util.PlaceholderUtils;

/**
 * @author Kevin Seim
 */
public class Settings {

  private static Properties props = new Properties();
  static {
    loadConfiguration();
    resolvePlaceholders();
  }
  
  private Settings() { }
  
  public static boolean getBoolean(String key) {
    return ensureNotNull(key, getBoolean(key, null));
  }
  
  public static Boolean getBoolean(String key, Boolean defaultValue) {
    String value = getString(key);
    return value == null ? defaultValue : ("1".equals(value) || "true".equalsIgnoreCase(value));
  }
  
  public static long getLong(String key) {
    return ensureNotNull(key, getLong(key, null));
  }
  
  public static Long getLong(String key, Long defaultValue) {
    String value = getString(key);
    if (value  == null) {
      return defaultValue;
    }
    try {
      return Long.valueOf(value);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid value for key '" + key + "'");
    }
  }

  public static String getString(String key) {
    return getString(key, null);
  }
  
  public static String getString(String key, String defaultValue) {
    String value = props.getProperty(key);
    return value == null ? defaultValue : value;
  }
  
  private static <T> T ensureNotNull(String key, T value) {
    if (value == null) {
      throw new IllegalStateException("Missing configuration setting '" + key + "'");
    }
    return value;
  }
  
  private static void loadConfiguration() {
    InputStream is = null;
    try {
      is = Settings.class.getResourceAsStream("/haven.properties");
      if (is == null) {
        throw new IllegalStateException("'haven.properties' not found on classpath");
      }
      props.load(is);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load default configuration", e);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }
  
  private static void resolvePlaceholders() {
    Enumeration<Object> keys = props.keys();
    while (keys.hasMoreElements()) {
      String key = (String) keys.nextElement();
      String value = props.getProperty(key);
      String resolved = PlaceholderUtils.resolve(value, props);
      if (value != resolved) {
        props.setProperty(key, resolved);
      }
    }
  }
}
