package org.seim.haven.commands;

import org.seim.haven.models.Token;
import org.seim.haven.store.ReplayState;

/**
 * @author Kevin Seim
 */
public interface Command {
  
  public String getName();
  
  public boolean isProcessed();
  
  public Request parse(Token... args);
  
  public boolean replay(ReplayState state, Token[] tokens);
  
}