package org.seim.haven.commands.blobs;

import org.seim.haven.commands.BasicCommand;
import org.seim.haven.models.Blob;
import org.seim.haven.models.Model;
import org.seim.haven.models.Token;
import org.seim.haven.models.ModelType;
import org.seim.haven.response.ErrorResponse;
import org.seim.haven.response.IntegerResponse;
import org.seim.haven.response.Response;
import org.seim.haven.store.Database;

/**
 * @author Kevin Seim
 */
public class Strlen extends BasicCommand {

  public Strlen() {
    setArgumentsLength(2, 2);
  }
  
  @Override
  protected Response process(Token[] tokens) {
    Model model = Database.get(tokens[1]);
    if (model == null) {
      return Response.ZERO;
    } else if (model.type() != ModelType.BLOB) {
      return new ErrorResponse("Invalid string key");
    } else {
      return new IntegerResponse(((Blob)model).getToken().value().length);
    }
  }
}
