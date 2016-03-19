package org.seim.haven.commands.blobs;

import org.seim.haven.commands.InvalidRequestException;
import org.seim.haven.commands.impl.Argument;
import org.seim.haven.commands.impl.FlexCommand;
import org.seim.haven.commands.impl.FlexRequest;
import org.seim.haven.commands.impl.Option;
import org.seim.haven.models.Blob;
import org.seim.haven.models.Counter;
import org.seim.haven.models.Model;
import org.seim.haven.models.ModelType;
import org.seim.haven.models.Token;
import org.seim.haven.response.IntegerResponse;
import org.seim.haven.response.Response;
import org.seim.haven.store.Database;
import org.seim.haven.store.ReplayState;

/**
 * @author Kevin Seim
 */
public abstract class CounterMutation extends FlexCommand {

  protected final Option xOpt = new Option("x", Option.Type.NUMBER);
  protected final Option xnOpt = new Option("xn");
  
  protected final Argument keyArg = new Argument("key", 0);
  
  public CounterMutation() { 
    this(null);
  }
  
  public CounterMutation(String name) {
    super(name);
    setOptions(xOpt, xnOpt);
  }

  @Override
  protected Response process(FlexRequest request) {
    return new IntegerResponse(mutate(request));
  }
  
  @Override
  protected boolean replay(ReplayState state, FlexRequest request) {
    Token key = request.getToken(keyArg);
    if (state.shouldReplay(key)) {
      mutate(request);
      return true;
    }
    return false;
  }

  protected long mutate(FlexRequest request) {
    Token key = request.getToken(keyArg);
    Model model = Database.get(key);
    long amount = getAmount(request);
    
    boolean added = false;
    if (model == null) {
      model = new Counter(amount);
      Database.put(key, model);
      added = true;
    }
    else if (model.type() == ModelType.BLOB) {
      // convert Blob's to counters...
      try {
        Counter counter = new Counter(((Blob)model).getToken().toLong());
        counter.setExpirationTime(model.getExpirationTime());
        counter.add(amount);
        model = counter;
        Database.put(key, model);
      } catch (NumberFormatException e) {
        throw new InvalidRequestException("Operation not supported for key");
      }
    }
    else if (model.type() == ModelType.COUNTER) {
      ((Counter)model).add(amount);
    }
    else {
      throw new InvalidRequestException("Operation not supported for key");
    }
    
    Token expires = request.getToken(xOpt);
    if (expires != null) {
      if (!request.has(xnOpt) || added) {
        model.setExpirationTime(expires.toLong() + System.currentTimeMillis());
      }
    }
    
    Database.log(request.getTokens());
    return ((Counter)model).getValue();
  }
  
  protected abstract long getAmount(FlexRequest request);
}
