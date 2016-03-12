package org.seim.haven.response;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;

public interface Response {

  public static final Charset UTF8 = Charset.forName("UTF-8");
  
  public static Response OK = new StringResponse("OK");
  public static Response QUEUED = new StringResponse("QUEUED");
  public static Response TRUE = new IntegerResponse(1);
  public static Response FALSE = new IntegerResponse(0);
  public static Response ZERO = FALSE;
  public static Response NIL = NilResponse.getInstance();
  
  /**
   * Writes the model to the given buffer.
   * @param buf the {@link ByteBuffer} to write to
   */
  public void serialize(ByteBuf buf);
  
  /**
   * Returns the maximum size of the model in bytes.
   * @return the number of bytes used to store the model
   */
  public int getSerializedLength();
  
}
