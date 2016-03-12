package org.seim.haven.commands.transactions;

import java.util.List;

import org.seim.haven.commands.InvalidRequestException;
import org.seim.haven.commands.Request;
import org.seim.haven.response.ArrayResponse;
import org.seim.haven.response.ErrorResponse;
import org.seim.haven.response.Response;

/**
 * @author Kevin Seim
 */
public class MultiRequest implements Request {

  private List<Request> requests;
  
  public void add(Request request) {
    this.requests.add(request);
  }
  
  @Override
  public Response call() throws Exception {
    Response[] responses = new Response[requests.size()];
    
    int i = 0;
    for (Request request : requests) {
      try {
        responses[i] = request.call();
      } catch (InvalidRequestException e) {
        responses[i] = new ErrorResponse(e.getMessage());
      } finally {
        ++i;
      }
    }
    
    return new ArrayResponse(responses);
  }

}
