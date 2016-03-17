package org.seim.haven.commands.keys;

import org.seim.haven.commands.impl.BasicCommand;
import org.seim.haven.models.Token;
import org.seim.haven.response.Response;
import org.seim.haven.store.Database;
import org.seim.haven.store.ReplayState;

public class Persist extends BasicCommand {

  public Persist() {
    setArgumentsLength(2, 2);
  }
  
  @Override
  protected Response process(Token[] tokens) {
    return process(ReplayState.LIVE, tokens) ? Response.TRUE : Response.FALSE;
  }

  @Override
  public boolean replay(ReplayState state, Token[] tokens) {
    return process(state, tokens);
  }
  
  private boolean process(ReplayState state, Token[] tokens) {
    Token key = tokens[1];
    return state.shouldReplay(key) ? Database.persist(key) : false;
  }
}
