package org.seim.haven.commands.blobs;

import org.seim.haven.commands.impl.FlexRequest;

/**
 * Increments a Counter model by one.
 * 
 * <p>Syntax: INCR <i>key</i>
 * 
 * @author Kevin Seim
 */
public final class Incr extends CounterMutation {

  public Incr() {
    setArguments(keyArg);
  }

  @Override
  protected long getAmount(FlexRequest request) {
    return 1;
  }
}
