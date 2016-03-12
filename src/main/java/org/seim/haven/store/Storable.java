package org.seim.haven.store;

import java.nio.ByteBuffer;

/**
 * @author Kevin Seim
 */
public interface Storable {

  /**
   * 
   * @param buf
   * @return
   */
  public int deserialize(ByteBuffer buf, int version); 
  
  /**
   * Writes the model to the given buffer.
   * @param buf the {@link ByteBuffer} to write to
   */
  public void serialize(ByteBuffer buf);
  
  /**
   * Returns the maximum size of the model in bytes.
   * @return the number of bytes used to store the model
   */
  public int getSerializedLength();
  
}
