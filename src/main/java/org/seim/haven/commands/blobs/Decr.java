package org.seim.haven.commands.blobs;

import org.seim.haven.commands.impl.FlexRequest;

/**
 * Decrements a Counter model by one.
 * 
 * <p>Syntax: DECR <i>key</i>
 * 
 * @author Kevin Seim
 */
public final class Decr extends CounterMutation {

  public Decr() {
    setArguments(keyArg);
  }

  @Override
  protected long getAmount(FlexRequest request) {
    return -1;
  }
}
