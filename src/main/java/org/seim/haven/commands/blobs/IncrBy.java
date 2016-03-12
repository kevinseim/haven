package org.seim.haven.commands.blobs;

import org.seim.haven.models.Token;

/**
 * Increments a Counter model by a given amount.
 * 
 * <p>Syntax: INCRBY <i>key</i> <i>amount</i>
 * 
 * @author Kevin Seim
 */
public final class IncrBy extends CounterMutation {

  public IncrBy() {
    setArgumentsLength(3, 3);
  }

  @Override
  protected long getAmount(Token[] tokens) {
    return tokens[2].toLong();
  }
}
