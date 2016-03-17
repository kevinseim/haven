package org.seim.haven.commands.keys;

import org.seim.haven.commands.impl.BasicCommand;
import org.seim.haven.models.Token;
import org.seim.haven.response.IntegerResponse;
import org.seim.haven.response.Response;
import org.seim.haven.store.Database;
import org.seim.haven.store.ReplayState;

/**
 * @author Kevin Seim
 */
public class Del extends BasicCommand {

  public Del() {
    super();
    setMinArgumentsLength(2);
  }
  
  @Override
  protected Response process(Token[] tokens) {
    return new IntegerResponse(process(ReplayState.LIVE, tokens));
  }
  
  @Override
  public boolean replay(ReplayState state, Token[] tokens) {
    return process(state, tokens) > 0;
  }
  
  private int process(ReplayState state, Token[] tokens) {
    int deleted = 0;
    
    for (int i=1; i<tokens.length; i++) {
      Token key = tokens[i];
      if (state.shouldReplay(key)) {
        Database.delete(key);
        ++deleted;
      }
    }
    
    return deleted;
  }
}
