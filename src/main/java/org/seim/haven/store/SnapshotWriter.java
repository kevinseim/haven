package org.seim.haven.store;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.seim.haven.models.Model;
import org.seim.haven.models.ModelType;
import org.seim.haven.models.Token;
import org.seim.haven.util.Charsets;
import org.seim.haven.util.FutureImpl;
import org.seim.haven.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;

/**
 * @author Kevin Seim
 */
final class SnapshotWriter implements Callable<Integer> {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(SnapshotWriter.class);
  
  private static final byte[] HEADER = "HAVEN".getBytes(Charsets.ASCII);
  
  private final File file;
  private final Accessor accessor;
  private final Expires expires = new Expires();
  private final int bufferSize;
  
  private transient Revision revision;
  private transient ByteBuffer buffer;
  private transient ByteBuffer uncompressedBuffer;
  private transient byte[] compressedBuffer;
  private transient Token key;
  private transient long keysWritten = 0;
  private transient long bytesWritten = 0;
  private transient Iterator<Token> keys = null;
  private transient Long startingRevision;
  private transient long lastRevision;
  
  public SnapshotWriter(File file, int bufferSize) throws IOException {
    this(file, null, bufferSize);
  }
  
  public SnapshotWriter(File file, Accessor accessor, int bufferSize) throws IOException {
    this.file = file;
    this.bufferSize = bufferSize;
    
    if (accessor != null) {
      this.accessor = accessor;
    } 
    else {
      this.accessor = new Accessor() {
        public <T> Future<T> submit(Callable<T> callable) throws ExecutionException {
          try {
            return new FutureImpl<T>(callable);
          } catch (Exception e) {
            throw new ExecutionException(e);
          }
        }
      };
    }
  }
  
  public Long getStartingRevision() {
    return startingRevision;
  }
  
  public long getKeysWritten() {
    return keysWritten;
  }
  
  public long getBytesWritten() {
    return bytesWritten;
  }
  
  public void write(Revision revision) throws IOException {
    this.revision = revision;
    
    FileOutputStream fout = new FileOutputStream(file);
    try {
      FileChannel channel = fout.getChannel();
      
      uncompressedBuffer = ByteBuffer.allocate(bufferSize);
      // TODO is it safe to assume compressed is always less?
      compressedBuffer = new byte[bufferSize];
      
      buffer = ByteBuffer.allocateDirect(bufferSize + 5);
      buffer.put(HEADER);
      buffer.putInt(1); // the snapshot version
      buffer.putInt(bufferSize); // minimum buffer size needed to read the snapshot
      
      try {
        while (accessor.submit(this).get() > 0) {
          writeUncompressedBuffer(channel);
        }
      } catch (InterruptedException e) {
        throw new IOException("Snapshot interrupted", e);
      } catch (ExecutionException e) {
        throw new IOException("Snapshot failed reading database", e.getCause());
      }
      
      if (uncompressedBuffer.position() > 0) {
        writeUncompressedBuffer(channel);
      }
      
      ensureRemainingCapacity(channel, 1);
      buffer.put(ModelType.RESERVED_EOF.getId());
      
      buffer.flip();
      while (buffer.hasRemaining()) {
        channel.write(buffer);
      }
      bytesWritten = channel.size();
      channel.close();
      fout.close();
      fout = null;
    }
    finally {
      if (fout != null) {
        IOUtils.closeQuietly(fout);
      }
      buffer = null;
    }
  }
  
  private void writeUncompressedBuffer(FileChannel channel) throws IOException {
    int compressedLength = Snappy.compress(
        uncompressedBuffer.array(), 0, uncompressedBuffer.position(), 
        compressedBuffer, 0);
    
    ensureRemainingCapacity(channel, compressedLength + 5);
    buffer.put(ModelType.RESERVED_COMPRESSED_BLOCK.getId());
    buffer.putInt(compressedLength);
    buffer.put(compressedBuffer, 0, compressedLength);
    //log.info("Writing {}b chunk", compressedLength);
    
    uncompressedBuffer.clear();
  }
  
  private void ensureRemainingCapacity(FileChannel channel, int expected) throws IOException {
    int remainingCapacity = buffer.remaining();
    if (remainingCapacity < expected) {
      int overflow = expected - remainingCapacity;
      buffer.flip();
      int written = 0;
      while ((written += channel.write(buffer)) < overflow);
      buffer.compact();
    }
  }

  @Override
  public Integer call() throws IOException {
    final ByteBuffer buffer = this.uncompressedBuffer;
    final Revision revision = this.revision;
    
    Token key = this.key;

    if (keys == null) {
      keys = Database.keys().iterator();
      if (keys.hasNext()) {
        key = keys.next();
      } else {
        return 0;
      }
      startingRevision = revision.getValue();
    }
    
    // write the current revision if changed
    if (lastRevision != revision.getAndWatch()) {
      int overflow = revision.getSerializedLength() + 1 - buffer.remaining();
      if (overflow > 0) {
        return overflow;
      }
      buffer.put(ModelType.REVISION.getId());
      revision.serialize(buffer);
      lastRevision = revision.getValue();
    }
    
    while (true) {
      Model model = Database.get(key);
      if (model == null) {
        continue;
      }
      
      int overflow = key.getSerializedLength() + model.getSerializedLength() + 1 - buffer.remaining();
      if (model.getExpirationTime() != null) {
        overflow += 1 + expires.getSerializedLength();
      }
      
      if (overflow > 0) {
        this.key = key;
        return overflow;
      }
      
      if (model.getExpirationTime() != null) {
        buffer.put(ModelType.RESERVED_EXPIRES.getId());
        expires.setExpires(model.getExpirationTime());
        expires.serialize(buffer);
      }
      
      buffer.put(model.type().getId());
      key.serialize(buffer);
      model.serialize(buffer);
      ++keysWritten;
      
      if (!keys.hasNext()) {
        break;
      } 
      
      key = keys.next();
    }
    
    return 0;
  }
}
