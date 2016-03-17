package org.seim.haven.commands.blobs;

import org.seim.haven.commands.InvalidRequestException;
import org.seim.haven.commands.Request;
import org.seim.haven.commands.impl.BasicCommand;
import org.seim.haven.models.Blob;
import org.seim.haven.models.Token;
import org.seim.haven.response.Response;
import org.seim.haven.store.Database;
import org.seim.haven.store.ReplayState;

/**
 * @author Kevin Seim
 */
public class Set extends BasicCommand {
  
  public Set() { 
    setArgumentsLength(3, 3);
  }
  
  @Override
  public Request parse(Token... tokens) {
    if (tokens.length != 3) {
      throw new InvalidRequestException("Wrong number of arguments");
    }
    return super.parse(tokens);
  }

  @Override
  protected Response process(Token[] tokens) {
    replay(ReplayState.LIVE, tokens);
    return Response.OK;
  }
  
  @Override
  public boolean replay(ReplayState state, Token[] tokens) {
    Token key = tokens[1];
    if (state.shouldReplay(key)) {
      Token value = tokens[2];
      Database.put(key, new Blob(value));
      Database.log(tokens);
      return true;
    }
    return false;
  }
}
