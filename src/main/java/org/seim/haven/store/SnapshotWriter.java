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

import org.seim.haven.Settings;
import org.seim.haven.models.Model;
import org.seim.haven.models.ModelType;
import org.seim.haven.models.Token;
import org.seim.haven.util.Charsets;
import org.seim.haven.util.FutureImpl;
import org.seim.haven.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kevin Seim
 */
final class SnapshotWriter implements Callable<Integer> {

  public static final byte[] HEADER = "HAVEN".getBytes(Charsets.ASCII);
  
  private static final int BUFFER_SIZE = (int) Settings.getLong("snapshot.buffer");
  
  private static final Logger log = LoggerFactory.getLogger(SnapshotWriter.class);

  private final File file;
  private final Accessor accessor;
  private final Expires expires = new Expires();
  
  private transient Revision revision;
  private transient ByteBuffer buffer;
  private transient Token key;
  private transient long keysWritten = 0;
  private transient long bytesWritten = 0;
  private transient Iterator<Token> keys = null;
  private transient Long startingRevision;
  private transient long lastRevision;
  
  public SnapshotWriter(File file) throws IOException {
    this(file, null);
  }
  
  public SnapshotWriter(File file, Accessor accessor) throws IOException {
    this.file = file;
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
      
      buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
      buffer.put(HEADER);
      buffer.putInt(1); // version
      
      try {
        int n = 0;
        while ((n = accessor.submit(this).get()) > 0) { 
          buffer.flip();
          while (channel.write(buffer) < n);
          buffer.compact();
        }
      } catch (InterruptedException e) {
        log.error("Error taking snapshot, shutting down", e.getCause());
        System.exit(1);
      } catch (ExecutionException e) {
        log.error("Error taking snapshot, shutting down", e.getCause());
        System.exit(1);
      }
      
      if (buffer.remaining() < 1) {
        buffer.flip();
        while (channel.write(buffer) < 1);
        buffer.compact();
      }
      
      buffer.put((byte)0xFF);
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

  @Override
  public Integer call() throws IOException {
    final ByteBuffer buffer = this.buffer;
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
        buffer.put(ModelType.EXPIRES.getId());
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
