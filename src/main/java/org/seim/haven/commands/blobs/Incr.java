package org.seim.haven.commands.blobs;

import org.seim.haven.models.Token;

/**
 * Increments a Counter model by one.
 * 
 * <p>Syntax: INCR <i>key</i>
 * 
 * @author Kevin Seim
 */
public final class Incr extends CounterMutation {

  public Incr() {
    setArgumentsLength(2, 2);
  }

  @Override
  protected long getAmount(Token[] tokens) {
    return 1;
  }
}
