package org.seim.haven.store;

import java.nio.ByteBuffer;

/**
 * @author Kevin Seim
 */
class Expires implements Storable {

  private long expires;
  
  public Expires() { }
  
  public void setExpires(long expires) {
    this.expires = expires;
  }
  
  public long getExpires() {
    return expires;
  }
  
  @Override
  public int deserialize(ByteBuffer buf, int version) {
    if (buf.remaining() < 8) {
      return 8 - buf.remaining();
    }
    expires = buf.getLong();
    return 0;
  }

  @Override
  public void serialize(ByteBuffer buf) {
    buf.putLong(expires);
  }

  @Override
  public int getSerializedLength() {
    return 8;
  }
}