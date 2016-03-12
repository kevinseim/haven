package org.seim.haven.commands.keys;

import org.seim.haven.commands.BasicCommand;
import org.seim.haven.models.Model;
import org.seim.haven.models.Token;
import org.seim.haven.response.IntegerResponse;
import org.seim.haven.response.Response;
import org.seim.haven.store.Database;

/**
 * @author Kevin Seim
 */
public class TOE extends BasicCommand {

  public TOE() {
    setArgumentsLength(2, 2);
  }
  
  @Override
  protected Response process(Token[] tokens) {
    Model model = Database.get(tokens[1]);
    if (model == null) {
      return new IntegerResponse(-2);
    } else if (model.getExpirationTime() == null) {
      return new IntegerResponse(-1);
    } else {
      return new IntegerResponse(model.getExpirationTime());
    }
  }
}
