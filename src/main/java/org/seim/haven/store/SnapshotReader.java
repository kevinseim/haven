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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

/**
 * @author Kevin Seim
 */
final class SnapshotReader implements Closeable {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(SnapshotReader.class);
  
  private static final byte[] HEADER = "HAVEN".getBytes(Charsets.ASCII);
  
  private final Expires expires = new Expires();
  private final Revision revision = new Revision();
  
  private FileInputStream fin;
  private FileChannel channel;
  private ByteBuffer buffer; 
  private int version;

  public SnapshotReader(File file, int bufferSize) throws IOException {
    fin = new FileInputStream(file);
    channel = fin.getChannel();
    buffer = ByteBuffer.allocateDirect(bufferSize);
  }
  
  /**
   * Loads the last saved snapshot.
   * @param revisions a Map of keys with their stored revision number
   * @return the most recent revision read from the snapshot
   * @throws IOException
   */
  public long load(Map<Token,Long> revisions) throws IOException {
    long rev = -1;
    
    readBytes(13);
    readHeader(buffer);
    version = readVersion(buffer);
    int bufferSize = buffer.getInt();

    ByteBuffer uncompressedBuffer = ByteBuffer.allocate(bufferSize);
    byte[] compressedBuffer = new byte[bufferSize];
    
    int compressedLength = 0;
    while ((compressedLength = readCompressedChunk(compressedBuffer)) != 0) {
    
      // uncompress the chunk
      uncompressedBuffer.clear();
      int size = Snappy.uncompress(compressedBuffer, 0, compressedLength, uncompressedBuffer.array(), 0);
      uncompressedBuffer.position(size);
      uncompressedBuffer.flip();
      
      // load the database from the uncompressed buffer
      while (uncompressedBuffer.hasRemaining()) {
        Storable s = readType(uncompressedBuffer);
        if (s == revision) {
          revision.deserialize(uncompressedBuffer, version);
          rev = revision.getValue();
          continue;
        }
        
        Long expirationTime = null;
        if (s == expires) {
           expires.deserialize(uncompressedBuffer, version);
           expirationTime = expires.getExpires();
           s = readType(uncompressedBuffer);
        }
        
        Token key = new Token();
        key.deserialize(uncompressedBuffer, version);
        
        Model model = (Model) s;
        model.deserialize(uncompressedBuffer, version);
        if (expirationTime != null) {
          model.setExpirationTime(expirationTime);
        }
        Database.put(key, model);
        revisions.put(key, rev);
      }
    }
    
    return rev;
  }
  
  private void readHeader(ByteBuffer buffer) throws IOException {
    byte[] header = new byte[5];
    buffer.get(header);
    if (!Arrays.equals(header, HEADER)) {
      throw new IOException("Invalid snapshot header");
    }
  }
  
  private int readVersion(ByteBuffer buffer) throws IOException {
    int version = buffer.getInt();
    if (version != 1) {
      throw new IOException("Version not supported");
    }
    return version;
  }
  
  private int readCompressedChunk(byte[] compressedBuffer) throws IOException {

    // read the chunk type
    if (buffer.remaining() < 1) {
      readBytes(1);
    }
    byte typeId = buffer.get();
    if (typeId == ModelType.RESERVED_EOF.getId()) {
      return 0;
    } else if (typeId != ModelType.RESERVED_COMPRESSED_BLOCK.getId()) {
      throw new IOException("Inalid file format, expected compressed block");
    }
    
    // read the compressed chunk size
    if (buffer.remaining() < 4) {
      readBytes(4);
    }
    int size = buffer.getInt();
    
    int needed = size - buffer.remaining();
    //log.info("Reading {}b chunk, {} needed", size, needed);
    if (needed > 0) {
      readBytes(needed);
    }
    
    buffer.get(compressedBuffer, 0, size);
    return size;
  }
  
  private Storable readType(ByteBuffer buffer) throws IOException {
    
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
    case RESERVED_EXPIRES:
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
