package org.seim.haven.commands.blobs;

import org.seim.haven.commands.impl.BasicCommand;
import org.seim.haven.models.Blob;
import org.seim.haven.models.Counter;
import org.seim.haven.models.Model;
import org.seim.haven.models.Token;
import org.seim.haven.models.ModelType;
import org.seim.haven.response.BlobResponse;
import org.seim.haven.response.ErrorResponse;
import org.seim.haven.response.Response;
import org.seim.haven.store.Database;

public final class Get extends BasicCommand {

  public Get() { 
    setArgumentsLength(2, 2);
  }

  @Override
  protected Response process(Token[] tokens) {
    Token key = tokens[1];
    Model model = Database.get(key);
    if (model == null) {
      return Response.NIL;
    } else if (model.type() == ModelType.BLOB) {
      return new BlobResponse(((Blob)model).getToken());
    } else if (model.type() == ModelType.COUNTER) {
      return new BlobResponse(((Counter)model).toString());
    } else {
      return new ErrorResponse("Invalid key type");
    }
  }
}
