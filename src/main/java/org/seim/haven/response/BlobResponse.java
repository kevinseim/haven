package org.seim.haven.response;

import org.seim.haven.models.Token;
import org.seim.haven.util.Charsets;

import io.netty.buffer.ByteBuf;

/**
 * @author Kevin Seim
 */
public class BlobResponse implements Response {

  private byte[] size;
  private byte[] value;
  
  public BlobResponse(Token token) {
    this(token.value());
  }
  
  public BlobResponse(String value) {
    this(value.getBytes(Charsets.UTF8));
  }
  
  public BlobResponse(byte[] value) {
    this.value = value;
    this.size = Charsets.getBytes(this.value.length);
  }
  
  public byte[] value() {
    return value;
  }
  
  @Override
  public void serialize(ByteBuf buf) {
    buf.writeByte('$');
    buf.writeBytes(size);
    buf.writeByte('\r');
    buf.writeByte('\n');
    buf.writeBytes(value);
    buf.writeByte('\r');
    buf.writeByte('\n');
  }

  @Override
  public int getSerializedLength() {
    return 5 + size.length + value.length;
  }
  
  @Override
  public String toString() {
    return new String(value, Charsets.UTF8);
  }
}
