package org.seim.haven.commands.impl;

import java.util.Arrays;

import org.seim.haven.commands.Command;
import org.seim.haven.commands.InvalidRequestException;
import org.seim.haven.models.Token;
import org.seim.haven.response.Response;
import org.seim.haven.store.ReplayState;

/**
 * @author Kevin Seim
 */
public abstract class FlexCommand implements Command {
  
  private static final int OPTION = 0;
  private static final int PARAM = 1;
  private static final int OPTION_END = 2;
  
  private final String name;
  private Option[] options;
  private Argument[] arguments;
  private int minArgumentLength = 0;
  private int maxArgumentLength = 0;
  
  public FlexCommand() {
    this(null);
  }
  
  public FlexCommand(String name) {
    if (name == null) {
      name = getClass().getSimpleName().toLowerCase();
    }
    this.name = name;
  }
  
  @Override
  public final String getName() {
    return name;
  }

  @Override
  public boolean isProcessed() {
    return true;
  }
  
  @Override
  public final FlexRequest parse(Token... args) {
    
    int index = 1;
    
    if (options != null) {
      
      loop: for (index = 1; index<args.length; index++) {
        switch (isOption(args[index])) {
        case OPTION:
          break;
        case OPTION_END:
          index++;
          // intentional fall through
        case PARAM:
          break loop;
        }
        
        Option matchedOption = null;
        for (Option option : options) {
          if (option.getToken().equals(args[index])) {
            // "intern" matches for future lookup performance
            args[index] = option.getToken();
            matchedOption = option;
          }
        }
        if (matchedOption == null) {
          throw new InvalidRequestException("Invalid option: " + args[index]);
        }
        
        if (matchedOption.hasParameter()) {
          // validate non-repeating options only appears once
          if (!matchedOption.isRepeating()) {
            for (int j=0; j<index; j++) {
              if (args[j] == matchedOption.getToken()) {
                throw new InvalidRequestException("Option does not allow multiple values: " + args[index]);  
              }
            }
          }
          
          // increment 'index' to skip the option value
          if (++index >= args.length) {
            throw new InvalidRequestException("Expected parameter for option '-" + matchedOption.getName() + "'");
          }
          
          if (matchedOption.getType() == Option.Type.NUMBER) {
            args[index].validateLong(matchedOption.getName());
          }
        }
      }
    }
    
    int argCount = args.length - index;
    if (argCount < minArgumentLength || argCount > maxArgumentLength) {
      throw new InvalidRequestException("Wrong number of arguments for command '" + getName() + "'");
    }
    
    FlexRequest request = new FlexRequest(this, args, index);
    validate(request);
    return request;
  }
  
  @Override
  public final boolean replay(ReplayState state, Token[] tokens) {
    return replay(state, parse(tokens));
  }
  
  protected void validate(FlexRequest request) {
    
  }
  
  /**
   * Process an a request parsed by this command.
   * @param request the {@link FlexRequest} to process
   * @return the {@link Response}
   */
  protected abstract Response process(FlexRequest request);
  
  /**
   * Replay an extended request.
   * @param state the {@link ReplayState}
   * @param request the {@link FlexRequest} to replay
   * @return true if the database was mutated, false otherwise
   */
  protected abstract boolean replay(ReplayState state, FlexRequest request);
  
  protected final void setOptions(Option...options) {
    this.options = options;
  }
  
  protected final Option[] getOptions() {
    return this.options;
  }
  
  protected final void setArguments(Argument...arguments) {
    Arrays.sort(arguments);
    
    int minArgumentLength = 0;
    int maxArgumentLength = 0;
    
    if (arguments != null && arguments.length > 0) {
      // validate indeterminate occurrences are only at the end
      for (int i=0; i<arguments.length; i++) {
        if (i > 0) {
          Argument prev = arguments[i - 1];
          if (prev.getMinOccurs() != prev.getMaxOccurs()) {
            throw new IllegalArgumentException("Invalid occurrences for argument '" + prev.getName() + "'");
          }
          minArgumentLength += prev.getMinOccurs();
        }
        Argument cur = arguments[i];
        if (cur.getOffset() != minArgumentLength) {
          throw new IllegalArgumentException("Invalid offset for argument '" + cur.getName() + "'");
        }
      }
      Argument last = arguments[arguments.length - 1];
      maxArgumentLength = last.getMaxOccurs() < Integer.MAX_VALUE ? 
          minArgumentLength + last.getMaxOccurs() : Integer.MAX_VALUE;
      minArgumentLength += last.getMinOccurs();
    }
    
    this.arguments = arguments;
    this.minArgumentLength = minArgumentLength;
    this.maxArgumentLength = maxArgumentLength;
  }
  
  protected final Argument[] getArguments() {
    return this.arguments;
  }
  
  protected static int isOption(Token token) {
    final byte[] value = token.value();
    final int length = value.length;
    if (length > 0) {
      if (value[0] == '-') {
        if (length == 2 && value[1] == '-') {
          return OPTION_END;
        } else {
          return OPTION;
        }
      }
    }
    return PARAM;
  }
}
