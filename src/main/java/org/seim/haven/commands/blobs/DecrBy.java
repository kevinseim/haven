package org.seim.haven.commands.blobs;

import org.seim.haven.commands.impl.Argument;
import org.seim.haven.commands.impl.FlexRequest;

/**
 * Decrements a Counter model by a given amount.
 * 
 * <p>Syntax: DECRBY <i>key</i> <i>amount</i>
 * 
 * @author Kevin Seim
 */
public final class DecrBy extends CounterMutation {

  protected final Argument amountArg = new Argument("amount", 1);
  
  public DecrBy() {
    setArguments(keyArg, amountArg);
  }

  @Override
  protected long getAmount(FlexRequest request) {
    return -request.getToken(amountArg).toLong();
  }
}
