package org.seim.haven.commands.keys;

import org.seim.haven.commands.impl.BasicCommand;
import org.seim.haven.models.Model;
import org.seim.haven.models.Token;
import org.seim.haven.response.Response;
import org.seim.haven.response.StringResponse;
import org.seim.haven.store.Database;

public class Type extends BasicCommand {

  public Type() {
    setArgumentsLength(2, 2);
  }
  
  @Override
  @SuppressWarnings("incomplete-switch")
  protected Response process(Token[] tokens) {
    Token key = tokens[1];
    Model model = Database.get(key);
    String type = "none";
    if (model != null) {
      switch (model.type()) {
      case BLOB:
        type = "string";
        break;
      }
    }
    return new StringResponse(type);
  }
}
