package org.seim.haven.commands.impl;

import org.seim.haven.models.Token;
import org.seim.haven.response.Response;

/**
 * @author Kevin Seim
 */
public class Noop extends BasicCommand {

  public Noop() {
    setArgumentsLength(1, 1);
  }

  public Noop(String name) { 
    super(name);
    setArgumentsLength(1, 1);
  }

  @Override
  protected Response process(Token[] tokens) {
    return null;
  }
}
