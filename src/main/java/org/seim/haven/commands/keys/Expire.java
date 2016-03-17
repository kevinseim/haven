package org.seim.haven.commands.keys;

import org.seim.haven.commands.impl.BasicCommand;
import org.seim.haven.models.Token;
import org.seim.haven.response.ErrorResponse;
import org.seim.haven.response.Response;
import org.seim.haven.store.Database;
import org.seim.haven.store.ReplayState;

/**
 * @author Kevin Seim
 */
public class Expire extends BasicCommand {

  private final boolean at;
  
  public Expire() {
    this(false);
  }
  
  public Expire(boolean at) {
    this.setArgumentsLength(3, 3);
    this.at = at;
  }
  
  @Override
  protected Response process(Token[] tokens) {
    try {
      long millis = tokens[2].toLong();
      if (millis <= 0) {
        return new ErrorResponse("Invalid millisecond value");
      }
      return expire(tokens[1], millis) ? Response.TRUE : Response.FALSE;
    } catch (NumberFormatException e) {
      return new ErrorResponse("Invalid millisecond value");
    }
  }

  @Override
  public boolean replay(ReplayState state, Token[] tokens) {
    Token key = tokens[1];
    return state.shouldReplay(key) ? expire(key, tokens[2].toLong()) : false;
  }
  
  private boolean expire(Token key, long millis) {
    if (!at) {
      millis += System.currentTimeMillis();
    }
    return Database.expire(key, millis);
  }
}
