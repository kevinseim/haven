package org.seim.haven.commands.blobs;

import org.seim.haven.commands.InvalidRequestException;
import org.seim.haven.commands.impl.BasicCommand;
import org.seim.haven.models.Blob;
import org.seim.haven.models.Counter;
import org.seim.haven.models.Model;
import org.seim.haven.models.ModelType;
import org.seim.haven.models.Token;
import org.seim.haven.response.IntegerResponse;
import org.seim.haven.response.Response;
import org.seim.haven.store.Database;
import org.seim.haven.store.ReplayState;

public abstract class CounterMutation extends BasicCommand {

  public CounterMutation() { }
  
  public CounterMutation(String name) {
    super(name);
  }
  
  @Override
  protected Response process(Token[] tokens) {
    return new IntegerResponse(mutate(tokens));
  }

  @Override
  public boolean replay(ReplayState state, Token[] tokens) {
    Token key = tokens[1];
    if (state.shouldReplay(key)) {
      mutate(tokens);
      return true;
    }
    return false;
  }
  
  protected long mutate(Token[] tokens) {
    Token key = tokens[1];
    Model model = Database.get(key);
    long amount = getAmount(tokens);
    
    if (model == null) {
      model = new Counter(amount);
      Database.put(key, model);
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
    
    Database.log(tokens);
    return ((Counter)model).getValue();
  }

  protected abstract long getAmount(Token[] tokens);
}
