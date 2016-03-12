package org.seim.haven.commands.transactions;

import org.seim.haven.commands.BasicCommand;
import org.seim.haven.commands.InvalidRequestException;
import org.seim.haven.commands.Request;
import org.seim.haven.models.Token;
import org.seim.haven.response.Response;

/**
 * @author Kevin Seim
 */
public class Multi extends BasicCommand {

  public Multi() { }

  @Override
  public Request parse(Token... tokens) {
    if (tokens.length != 1) {
      throw new InvalidRequestException("Wrong number of arguments for 'multi' command");
    }
    return new MultiRequest();
  }

  @Override
  protected Response process(Token[] tokens) {
    return null;
  }
}
