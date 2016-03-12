package org.seim.haven.commands;

import java.util.concurrent.Callable;

import org.seim.haven.response.Response;

/**

 * @author Kevin Seim
 */
public interface Request extends Callable<Response> {

  /*
  protected final Token[] tokens;
  
  public Request() {
    this.tokens = null;
  }
  
  public Request(Token...tokens) {
    this.tokens = tokens;
  }
  
  public Token[] getTokens() {
    return this.tokens;
  }
  
  public Token getToken(int i) {
    return this.tokens[i];
  }
  */
  
}
