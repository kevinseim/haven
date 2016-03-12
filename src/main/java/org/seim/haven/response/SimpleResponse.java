package org.seim.haven.response;

import io.netty.buffer.ByteBuf;

abstract class SimpleResponse implements Response {

  private final byte[] value;

  public SimpleResponse(String value) {
    this.value = value.getBytes(UTF8);
  }
  
  public SimpleResponse(byte[] value) {
    this.value = value;
  }
  
  protected abstract byte getIdentifier();
  
  public byte[] value() {
    return value;
  }

  @Override
  public void serialize(ByteBuf buf) {
    buf.writeByte(getIdentifier());
    buf.writeBytes(value);
    buf.writeByte('\r');
    buf.writeByte('\n');
  }

  @Override
  public int getSerializedLength() {
    return value.length + 3;
  }
  
  @Override 
  public String toString() {
    return new String(value, Response.UTF8);
  }
}
