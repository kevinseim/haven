package org.seim.haven.commands.blobs;

import org.seim.haven.commands.impl.Argument;
import org.seim.haven.commands.impl.FlexRequest;

/**
 * Increments a Counter model by a given amount.
 * 
 * <p>Syntax: INCRBY <i>key</i> <i>amount</i>
 * 
 * @author Kevin Seim
 */
public final class IncrBy extends CounterMutation {

  protected final Argument amountArg = new Argument("amount", 1);
  
  public IncrBy() {
    setArguments(keyArg, amountArg);
  }

  @Override
  protected long getAmount(FlexRequest request) {
    return request.getToken(amountArg).toLong();
  }
}
