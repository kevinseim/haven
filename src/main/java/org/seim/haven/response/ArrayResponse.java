package org.seim.haven.response;

import java.util.Arrays;
import java.util.List;

import io.netty.buffer.ByteBuf;

/**
 * @author Kevin Seim
 */
public class ArrayResponse implements Response {

  private byte[] size;
  private Response[] responses;
  private int serializedLength;
  
  public ArrayResponse(Response[] responses) {
    this.responses = responses;
    this.size = Integer.toString(responses.length).getBytes(UTF8);
    
    int length = 3 + size.length;
    for (Response response : responses) {
      length += response.getSerializedLength();
    }
    this.serializedLength = length;
  }
  
  public List<Response> getResponses() {
    return Arrays.asList(responses);
  }

  @Override
  public void serialize(ByteBuf buf) {
    buf.writeByte('*');
    buf.writeBytes(size);
    buf.writeByte('\r');
    buf.writeByte('\n');
    
    for (Response response : responses) {
      response.serialize(buf);
    }
  }

  @Override
  public int getSerializedLength() {
    return serializedLength;
  }

}
