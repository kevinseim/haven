package org.seim.haven.commands.blobs;

import org.seim.haven.models.Token;

/**
 * Decrements a Counter model by one.
 * 
 * <p>Syntax: DECR <i>key</i>
 * 
 * @author Kevin Seim
 */
public final class Decr extends CounterMutation {

  public Decr() {
    setArgumentsLength(2, 2);
  }

  @Override
  protected long getAmount(Token[] tokens) {
    return -1;
  }
}
