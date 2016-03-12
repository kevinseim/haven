package org.seim.haven.models;

import org.seim.haven.store.Storable;

/**
 * Interface for models stored in the database.
 * @author Kevin Seim
 */
public interface Model extends Storable {
  
  /**
   * Returns the model type.
   * @return the {@link ModelType}.
   */
  public ModelType type();
  
  /**
   * Returns the expiration time in milliseconds since the epoch.
   * @return expiration time, or null if this model does not expire
   */
  public Long getExpirationTime();

  /**
   * Sets the time to expire this model in milliseconds since the epoch
   * @param expirationTime the expiration time, or null if this model should persist
   */
  public void setExpirationTime(Long expirationTime);
  
  /**
   * Returns whether this model has expired.
   * @return true if expired, false otherwise
   */
  public default boolean isExpired() {
    return (getExpirationTime() != null && getExpirationTime() <= System.currentTimeMillis());
  }
}
