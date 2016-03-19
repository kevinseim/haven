package org.seim.haven.commands.blobs;

import org.seim.haven.commands.impl.Argument;
import org.seim.haven.commands.impl.FlexCommand;
import org.seim.haven.commands.impl.FlexRequest;
import org.seim.haven.commands.impl.Option;
import org.seim.haven.models.Blob;
import org.seim.haven.models.Token;
import org.seim.haven.response.Response;
import org.seim.haven.store.Database;
import org.seim.haven.store.ReplayState;

/**
 * @author Kevin Seim
 */
public class Set extends FlexCommand {
  
  private final Option xOpt = new Option("x", Option.Type.NUMBER);
  private final Option xnxOpt = new Option("xn");

  private final Argument keyArg = new Argument("key", 0);
  private final Argument valueArg = new Argument("value", 1);
  
  public Set() {
    setOptions(xOpt, xnxOpt);
    setArguments(keyArg, valueArg);
  }
  
  @Override
  protected Response process(FlexRequest request) {
    replay(ReplayState.LIVE, request);
    return Response.OK;
  }

  @Override
  protected boolean replay(ReplayState state, FlexRequest request) {
    Token key = request.getToken(keyArg);
    if (state.shouldReplay(key)) {
      // create a new Blob model
      Blob blob = new Blob(request.getToken(valueArg));
      
      // set the expiration time if specified
      Token expires = request.getToken(xOpt);
      if (expires != null) {
        long at = expires.toLong() + System.currentTimeMillis(); 
        if (!request.has(xnxOpt) || !Database.contains(key)) {
          blob.setExpirationTime(at);
        }
      }
      
      Database.put(key, blob);
      Database.log(request.getTokens());
      return true;
    }
    return false;
  }
}
