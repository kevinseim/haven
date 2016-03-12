package org.seim.haven.store;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Map;

import org.seim.haven.models.Blob;
import org.seim.haven.models.Counter;
import org.seim.haven.models.Model;
import org.seim.haven.models.ModelType;
import org.seim.haven.models.Token;
import org.seim.haven.util.Charsets;
import org.seim.haven.util.IOUtils;

/**
 * @author Kevin Seim
 */
final class SnapshotReader implements Closeable {

  private static final byte[] HEADER = "HAVEN".getBytes(Charsets.ASCII);
  
  private final Expires expires = new Expires();
  private final Revision revision = new Revision();
  
  private FileInputStream fin;
  private FileChannel channel;
  private ByteBuffer buffer; 
  private int version;
  
  public SnapshotReader(File file) throws IOException {
    fin = new FileInputStream(file);
    channel = fin.getChannel();
    buffer = ByteBuffer.allocateDirect(1 * 1024 * 1024);
  }
  
  /**
   * Loads the last saved snapshot.
   * @param revisions a Map of keys with their stored revision number
   * @return the most recent revision read from the snapshot
   * @throws IOException
   */
  public long load(Map<Token,Long> revisions) throws IOException {
    readBytes(9);
    version = readHeader(buffer);
    
    long rev = -1;
    
    Storable s;
    while ((s = readType()) != null) {
      if (s == revision) {
        read(revision);
        rev = revision.getValue();
        continue;
      }
      
      Long expirationTime = null;
      if (s == expires) {
        expirationTime = read(expires).getExpires();
        s = readType();
      }
      Token key = read(new Token());
      Model model = (Model) read(s);
      if (expirationTime != null) {
        model.setExpirationTime(expirationTime);
      }
      Database.put(key, model);
      revisions.put(key, rev);
    }
    
    return rev;
  }
  
  private int readHeader(ByteBuffer buffer) throws IOException {
    byte[] header = new byte[5];
    buffer.get(header);
    if (!Arrays.equals(header, HEADER)) {
      throw new IOException("Invalid snapshot header");
    }
    int version = buffer.getInt();
    if (version != 1) {
      throw new IOException("Version not supported");
    }
    return version;
  }
  
  private <T extends Storable> T read(T storable) throws IOException {
    int needed = storable.deserialize(buffer, version);
    while (needed > 0) {
      readBytes(needed);
      needed = storable.deserialize(buffer, version);
    }
    return storable;
  }
  
  private Storable readType() throws IOException {
    if (buffer.remaining() < 1) {
      readBytes(1);
    }
    
    byte typeId = buffer.get();
    if (typeId == (byte) 0xFF) {
      return null;
    }
    
    ModelType type = ModelType.fromId(typeId);
    switch (type) {
    case BLOB:
      return new Blob();
    case COUNTER:
      return new Counter();
    case EXPIRES:
      return expires;
    case REVISION:
      return revision;
    default:
      throw new IOException("Unidentified model type: " + typeId);
    }
  }
  
  private int readBytes(int min) throws IOException {
    int read = 0;
    while (read < min) {
      buffer.compact();
      int n = channel.read(buffer);
      if (n == -1) {
        throw new IOException("Unexpected EOF");
      }
      read += n;
    }
    buffer.flip();
    return read;
  }
  
  @Override
  public void close() {
    IOUtils.closeQuietly(channel);
    IOUtils.closeQuietly(fin);
    buffer = null;
    channel = null;
    fin = null;
  }
}
