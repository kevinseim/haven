package org.seim.haven.models;

import java.nio.ByteBuffer;

/**
 * @author Kevin Seim
 */
public class Counter extends AbstractModel {

  private long value;
  
  public Counter() { }
  
  public Counter(long value) {
    this.value = value;
  }
  
  public long getValue() {
    return value;
  }
  
  public void add(long delta) {
    value += delta;
  }
  
  @Override
  public ModelType type() {
    return ModelType.COUNTER;
  }

  @Override
  public int deserialize(ByteBuffer buf, int version) {
    if (buf.remaining() < 8) {
      return 8 - buf.remaining();
    } else {
      this.value = buf.getLong();
      return 0;
    }
  }

  @Override
  public void serialize(ByteBuffer buf) {
    buf.putLong(value);
  }

  @Override
  public int getSerializedLength() {
    return 8;
  }
  
  @Override
  public String toString() {
    return Long.toString(value);
  }
}
