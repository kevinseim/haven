package org.seim.haven.commands.impl;

import org.seim.haven.commands.Request;
import org.seim.haven.models.Token;
import org.seim.haven.response.Response;

/**
 * 
 * 
 * 
 * <p>See code below for efficient looping instructions over repeating 
 * arguments and options:
 * 
 * <pre>
 * int index = 0;
 * while ((index = req.indexOf(keys, index)) != -1) {
 *   Token token = req.getToken(index++);
 *   
 *   // do something with token
 * }
 * </pre>
 * 
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
  
  public Token getToken(int index) {
    return tokens[index];
  }
  
  public Token[] getTokens() {
    return tokens;
  }
  
  /*
  public Token getArgument(int index) {
    return tokens[parameterOffset + index];
  }
  */
  
  public Token getToken(Argument argument) {
    int offset = indexOf(argument);
    return (offset < 0) ? null : tokens[offset];
  }
  
  /**
   * @deprecated
   */
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
  
  public int indexOf(Argument argument) {
    int offset = parameterOffset + argument.getOffset();
    return (offset < tokens.length) ? offset : -1;
  }
  
  public int indexOf(Argument argument, int from) {
    int offset = parameterOffset + argument.getOffset();
    int index = from > offset ? from : offset;
    if (index < tokens.length) {
      int length = index - offset;
      if (length < argument.getMaxOccurs()) {
        return index;
      }
    }
    return -1;
  }
  
  public Token getToken(Option option) {
    return option.getValue(tokens, 1, parameterOffset);
  }
  
  /**
   * @deprecated
   */
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
