package org.seim.haven.commands.blobs;

import org.seim.haven.models.Token;

/**
 * Decrements a Counter model by a given amount.
 * 
 * <p>Syntax: DECRBY <i>key</i> <i>amount</i>
 * 
 * @author Kevin Seim
 */
public final class DecrBy extends CounterMutation {

  public DecrBy() {
    setArgumentsLength(3, 3);
  }
  
  @Override
  protected long getAmount(Token[] tokens) {
    return -tokens[2].toLong();
  }
}
