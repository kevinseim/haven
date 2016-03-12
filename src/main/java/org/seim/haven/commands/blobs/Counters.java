package org.seim.haven.commands.blobs;

import org.seim.haven.commands.InvalidRequestException;
import org.seim.haven.models.Blob;
import org.seim.haven.models.Counter;
import org.seim.haven.models.Model;
import org.seim.haven.models.ModelType;
import org.seim.haven.models.Token;
import org.seim.haven.store.Database;

/**
 * Helper methods for Counter models.
 * @author Kevin Seim
 */
public class Counters {

  private Counters() { }
  
  public static long incr(Token[] tokens, long amount) {
    Token key = tokens[1];
    Model model = Database.get(key);
    
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
}
