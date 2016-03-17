package org.seim.haven.commands.server;

import org.seim.haven.commands.impl.BasicCommand;
import org.seim.haven.models.Token;
import org.seim.haven.response.Response;
import org.seim.haven.response.StringResponse;

public class Ping extends BasicCommand {

  public static Response PONG = new StringResponse("PONG");
  
  public Ping() { 
    setArgumentsLength(1, 1);
  }
  
  @Override
  public boolean isProcessed() {
    return false;
  }
  
  @Override
  protected Response process(Token[] tokens) {
    return PONG;
  }
}
