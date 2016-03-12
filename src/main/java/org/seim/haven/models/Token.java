package org.seim.haven.models;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.seim.haven.store.Storable;
import org.seim.haven.util.Charsets;
import org.xerial.snappy.Snappy;

/**
 * @author Kevin Seim
 */
public final class Token implements Storable {
  
  private byte[] value;
  private transient Integer hashCode;

  public Token() { }
  
  public Token(long value) {
    this.value = Charsets.getBytes(value);
  }
  
  public Token(String value) {
    this(value.getBytes(Charsets.UTF8));
  }
  
  public Token(byte[] value) {
    this.value = value;
  }
  
  public Token toLowerCase() {
    byte[] lowercase = null;
    for (int i=0; i<value.length; i++) {
      if (value[i] >= 65 && value[i] <= 90) {
        if (lowercase == null) {
          lowercase = new byte[value.length];
          System.arraycopy(value, 0, lowercase, 0, i);
        }
        lowercase[i] = (byte) (value[i] + 32);
      }
      else if (lowercase != null) {
        lowercase[i] = value[i];
      }
    }
    return (lowercase != null) ? new Token(lowercase) : this;
  }
  
  public byte[] value() {
    return value;
  }
  
  @Override
  public void serialize(ByteBuffer buf) {
    byte[] data = this.value;
    int length = data.length;
    
    if (length > 1024) {
      try {
        byte[] cdata = Snappy.compress(data);
        if (cdata.length < 0.9 * length) {
          data = cdata;
          length = cdata.length;
          buf.put((byte) (0x1100_0100));
        }
      }
      catch (IOException e) { 
        // ignore and simply do not compress
      }
    }
    
    if (length < 63) {
      buf.put((byte) value.length);
    } else if (length < 16383) {
      buf.put((byte) (0x0100_0000 & length >> 8));
      buf.put((byte) length);
    } else {
      buf.put((byte) (0x1000_0000));
      buf.putInt(length);
    }
    buf.put(value);
  }
  
  @Override
  public int deserialize(ByteBuffer buf, int version) {
    if (buf.remaining() < 2) {
      return 2;
    }
    
    final int position = buf.position();
    int length;
    
    byte format = buf.get();
    switch (format >> 6) {
    case 0:
      length = format;
      break;
    // TODO handle other lengths
    default:
      length = 0;
    }
    
    if (buf.remaining() < length) {
      buf.position(position);
      return length - buf.remaining();
    }
    
    value = new byte[length];
    buf.get(value);
    return 0;
  }
  
  @Override
  public int getSerializedLength() {
    int length = value.length;
    if (length < 63) {
      return length + 1;
    } else if (length < 16383) {
      return length + 2;
    } else {
      return length + 5;
    }
  }
  
  @Override
  public int hashCode() {
    if (hashCode == null) {
      hashCode = Arrays.hashCode(value);
    }
    return hashCode;
  }
  
  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof Token)) {
      return false;
    }
    return Arrays.equals(this.value, ((Token)o).value);
  }
  
  public long toLong() throws NumberFormatException {
    if (value.length > 20) {
      throw new NumberFormatException("Value exceeds maximum size of a 64-bit integer");
    }
    StringBuilder sb = new StringBuilder();
    for (byte b : value) {
      sb.append((char)b);
    }
    return Long.valueOf(sb.toString());
  }
  
  @Override
  public String toString() {
    return new String(value, Charsets.UTF8);
  }
}
