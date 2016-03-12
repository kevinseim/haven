package org.seim.haven.commands.server;

import org.seim.haven.commands.BasicCommand;
import org.seim.haven.models.Token;
import org.seim.haven.response.Response;
import org.seim.haven.store.Database;
import org.seim.haven.store.ReplayState;

/**
 * @author Kevin Seim
 */
public class Flushall extends BasicCommand {

  public Flushall() { }

  @Override
  protected Response process(Token[] tokens) {
    Database.clear();
    Database.log(tokens);
    return Response.OK;
  }

  @Override
  public boolean replay(ReplayState state, Token[] tokens) {
    int deleted = 0;
    for (Token key : Database.keys()) {
      if (state.shouldReplay(key)) {
        Database.delete(key);
        ++deleted;
      }
    }
    return deleted > 0;
  }
}
