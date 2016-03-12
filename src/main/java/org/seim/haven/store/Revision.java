package org.seim.haven.store;

import java.nio.ByteBuffer;

import org.seim.haven.models.Model;
import org.seim.haven.models.ModelType;

/**
 * @author Kevin Seim
 */
public class Revision implements Model {

  private long revision;
  
  private transient boolean watch = true;
  
  public Revision() { }
  
  public Revision(long rev) {
    this.revision = rev;
  }
  
  public long getValue() {
    return revision;
  }
  
  public boolean incrementIfWatched() {
    if (watch) {
      revision++;
      watch = false;
      return true;
    }
    return false;
  }
  
  public long getAndWatch() {
    watch = true;
    return revision;
  }
  
  @Override
  public int deserialize(ByteBuffer buf, int version) {
    if (buf.remaining() < 8) {
      return 8 - buf.remaining();
    }
    revision = buf.getLong();
    return 0;
  }

  @Override
  public void serialize(ByteBuffer buf) {
    buf.putLong(revision);
  }

  @Override
  public int getSerializedLength() {
    return 8;
  }

  @Override
  public Long getExpirationTime() {
    return null;
  }

  @Override
  public void setExpirationTime(Long expirationTime) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ModelType type() {
    return ModelType.REVISION;
  }
}
