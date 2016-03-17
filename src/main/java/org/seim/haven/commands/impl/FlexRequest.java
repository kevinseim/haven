package org.seim.haven.commands.impl;

import org.seim.haven.commands.Request;
import org.seim.haven.models.Token;
import org.seim.haven.response.Response;

/**
 * @author Kevin Seim
 */
public class FlexRequest implements Request {

  private FlexCommand command;
  private Token[] tokens;
  private int parameterOffset;
  
  FlexRequest(FlexCommand command, Token[] tokens, int parameterOffset) {
    this.command = command;
    this.tokens = tokens;
    this.parameterOffset = parameterOffset;
  }
  
  public Token getParameter(int index) {
    return tokens[parameterOffset + index];
  }
  
  public Token getValue(Argument argument) {
    int offset = parameterOffset + argument.getOffset();
    return (offset < tokens.length) ? tokens[offset] : null;
  }
  
  public Token[] getValues(Argument argument) {
    int offset = parameterOffset + argument.getOffset();
    int length = tokens.length - offset;
    if (length > argument.getMaxOccurs()) {
      length = argument.getMaxOccurs();
    }
    Token[] values = new Token[length];
    System.arraycopy(tokens, offset, values, 0, length);
    return values;
  }
  
  public Token getValue(Option option) {
    return option.getValue(tokens, 1, parameterOffset);
  }
  
  public Token[] getValues(Option option) {
    return option.getValues(tokens, 1, parameterOffset).toArray(new Token[0]);
  }
  
  public boolean has(Option option) {
    return option.isPresent(tokens, 1, parameterOffset);
  }
  
  @Override
  public Response call() throws Exception {
    return command.process(this);
  }
}
