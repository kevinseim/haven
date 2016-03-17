package org.seim.haven.commands.impl;

import org.seim.haven.commands.Command;
import org.seim.haven.commands.InvalidRequestException;
import org.seim.haven.commands.Request;
import org.seim.haven.models.Token;
import org.seim.haven.response.ErrorResponse;
import org.seim.haven.response.Response;
import org.seim.haven.store.ReplayState;

/**
 * @author Kevin Seim
 */
public abstract class BasicCommand implements Command {

  private final String name;
  private Integer minArgumentsLength;
  private Integer maxArgumentsLength;
  
  public BasicCommand() {
    this.name = getClass().getSimpleName().toLowerCase();
  }
  
  public BasicCommand(String name) {
    this.name = name;
  }
  
  @Override
  public final String getName() {
    return name.toString();
  }
  
  @Override
  public boolean isProcessed() {
    return true;
  }

  public Request parse(final Token... tokens) {
    if (minArgumentsLength != null && tokens.length < minArgumentsLength) {
      throw new InvalidRequestException("wrong number of arguments for '" + getName() + "' command");
    }
    if (maxArgumentsLength != null && tokens.length > maxArgumentsLength) {
      throw new InvalidRequestException("wrong number of arguments for '" + getName() + "' command");
    }
    return new Request() {
      public Response call() throws Exception {
        try {
          return process(tokens);
        } catch (InvalidRequestException e) {
          return new ErrorResponse(e.getMessage());
        }
      }
    };
  }
  
  public boolean replay(ReplayState state, Token[] tokens) {
    throw new UnsupportedOperationException("Command '" + name + "' does not support replay");
  }

  protected abstract Response process(Token[] tokens);
  
  protected void setArgumentsLength(Integer min, Integer max) {
    setMinArgumentsLength(min);
    setMaxArgumentsLength(max);
  }
  
  protected void setMinArgumentsLength(Integer n) {
    this.minArgumentsLength = n;
  }
  
  protected void setMaxArgumentsLength(Integer n) {
    this.maxArgumentsLength = n;
  }
}
