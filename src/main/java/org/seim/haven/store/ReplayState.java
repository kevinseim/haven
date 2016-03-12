package org.seim.haven.store;

import org.seim.haven.models.Token;

/**
 * 
 * @author kevin
 */
public interface ReplayState {

  public final static ReplayState LIVE = new ReplayState() {
    @Override
    public boolean shouldReplay(Token key) {
      return true;
    }
  };
  
  /**
   * 
   * @param key
   * @return
   */
  public boolean shouldReplay(Token key);
  
}
