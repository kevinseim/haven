package org.seim.haven.store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.seim.haven.models.Model;
import org.seim.haven.models.Token;

/**
 * A singleton in memory hash table where all models are stored.
 * 
 * <p>The database is not thread safe.  All database access to should single
 * threaded or externally synchronized.
 * 
 * @author Kevin Seim
 */
public final class Database {

  private static final Token[] deleteTokens = new Token[] { new Token("del"), null };
  private static final Token[] persistTokens = new Token[] { new Token("persist"), null };
  private static final Token[] expireatTokens = new Token[] { new Token("expireat"), null, null };
  
  private static final Map<Token,Model> table = new HashMap<>();
  private static final Set<Token> expiring = new LinkedHashSet<>();
  
  private static Store store;
  private static Revision revision;
  private static boolean live;
  private static int lastIndex;
  
  private Database() { }
  
  /**
   * Returns a Set of all keys in the database.  The Set is a copy
   * and is not backed by the underlying table.
   * @return the Set of all keys
   */
  public static Set<Token> keys() {
    return new HashSet<>(table.keySet());
  }
  
  /**
   * Tests whether the given key exists.
   * @param key the key to test
   * @return true if it exists, false otherwise
   */
  public static boolean contains(Token key) {
    return get(key) != null;
  }
  
  /**
   * Returns the model for a given key.  If an expired model is
   * found, the key is deleted and null is returned.
   * @param key the model key
   * @return the model, or null if not found or expired
   */
  public static Model get(Token key) {
    Model model = table.get(key);
    if (model != null && model.isExpired()) {
      logDelete(key);
      expiring.remove(key);
      model = null;
    }
    return model;
  }
  
  /**
   * Sets the expiration time of a key.
   * @param key the key to expire
   * @param time the expiration time
   * @return true if set, false if the key did not exist
   */
  public static boolean expire(Token key, long time) {
    Model model = get(key);
    if (model == null) {
      return false;
    }
    model.setExpirationTime(time);
    expiring.add(key);
    expireatTokens[1] = key;
    expireatTokens[2] = new Token(time);
    log(expireatTokens);
    expireatTokens[1] = null;
    expireatTokens[2] = null;
    return true;
  }
  
  /**
   * Clears the expiration time for a given key.
   * @param key the key to persist
   * @return true if persisted, false if the key did not exist
   */
  public static boolean persist(Token key) {
    Model model = get(key);
    if (model == null) {
      return false;
    }
    if (model.getExpirationTime() != null) {
      model.setExpirationTime(null);
      expiring.remove(key);
      persistTokens[1] = key;
      log(persistTokens);
      persistTokens[1] = null;
    }
    return true;
  }
  
  /**
   * Stores a new model in the database.
   * @param key the key to store the model under
   * @param model the model to store
   */
  public static void put(Token key, Model model) {
    Model prev = table.put(key, model);
    if (model.getExpirationTime() != null) {
      if (prev == null || prev.getExpirationTime() == null) {
        expiring.add(key);
      }
    }
    else {
      if (prev != null && prev.getExpirationTime() != null) {
        expiring.remove(key);
      }
    }
  }
  
  /**
   * Deletes a model by key.
   * @param key the key to delete
   * @return the existing Model, or null if the key was not set
   */
  public static Model delete(Token key) {
    Model model = table.remove(key);
    if (model != null) {
      if (model.getExpirationTime() != null) {
        expiring.remove(key);
        model = null;
      }
      logDelete(key);
    }
    return model;
  }
  
  /**
   * Removes all keys from the database.
   */
  public static void clear() {
    table.clear();
    lastIndex = 0;
  }
  
  public static int size() {
    return table.size();
  }
  
  private static void logDelete(Token key) {
    deleteTokens[1] = key;
    log(deleteTokens);
    deleteTokens[1] = null;
  }
  
  public static void log(Token[] tokens) {
    if (live) {
      if (revision.incrementIfWatched()) {
        store.log(revision);
      }
      store.log(tokens);
    }
  }

  /**
   * Samples and removes expired keys.
   */
  public static void cleanupExpiredKeys() {
    int index = 0;
    int checked = 0;
    int expired = 0;
    
    Iterator<Token> iter = expiring.iterator();
    while (iter.hasNext()) {
      Token key = iter.next();
      
      if (++index > lastIndex) {
        Model model = table.get(key);
        if (model.isExpired()) {
          iter.remove();
          table.remove(key);
          logDelete(key);
          ++expired;
          --index;
        }
        if (++checked == 20) {
          if (expired < 5) {
            lastIndex = index;
            return;
          }
          checked = 0;
          expired = 0;        
        }
      }
    }
    lastIndex = 0;
  }

  static void setStore(Store store) {
    Database.store = store;
  }
  
  static void setRevision(Revision revision) {
    Database.revision = revision;
  }
  
  static void setLive(boolean live) {
    Database.live = live;
  }
  
  static Revision revision() {
    return revision;
  }
}
