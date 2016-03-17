package org.seim.haven.commands.blobs;

import org.seim.haven.commands.impl.BasicCommand;
import org.seim.haven.models.Blob;
import org.seim.haven.models.Counter;
import org.seim.haven.models.Model;
import org.seim.haven.models.Token;
import org.seim.haven.models.ModelType;
import org.seim.haven.response.ArrayResponse;
import org.seim.haven.response.BlobResponse;
import org.seim.haven.response.Response;
import org.seim.haven.store.Database;

/**
 * @author Kevin Seim
 */
public final class Mget extends BasicCommand {

  public Mget() {
    this.setMinArgumentsLength(2);
  }
  
  @Override
  protected Response process(Token[] tokens) {
    Response[] responses = new Response[tokens.length - 1];
    for (int i=1; i<tokens.length; i++) {
      Model model = Database.get(tokens[i]);
      if (model == null) {
        responses[i-1] = Response.NIL;
      } else if (model.type() == ModelType.BLOB) {
        responses[i-1] = new BlobResponse(((Blob)model).getToken());
      } else if (model.type() == ModelType.COUNTER) {
        responses[i-1] = new BlobResponse(((Counter)model).toString());
      } else {
        responses[i-1] = Response.NIL;
      }
    }
    return new ArrayResponse(responses);
  }
}
