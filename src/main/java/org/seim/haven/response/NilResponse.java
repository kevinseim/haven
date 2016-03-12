package org.seim.haven.response;

import io.netty.buffer.ByteBuf;

/**
 * @author Kevin Seim
 */
public class NilResponse implements Response {

  private static NilResponse instance = new NilResponse();
  private byte[] value = new byte[]{'$', '-', '1', '\r', '\n'};
  
  private NilResponse() { }
  
  public static NilResponse getInstance() {
    return instance;
  }
  
  @Override
  public void serialize(ByteBuf buf) {
    buf.writeBytes(value);
  }

  @Override
  public int getSerializedLength() {
    return 5;
  }
  
  @Override
  public String toString() {
    return "nil";
  }
}
