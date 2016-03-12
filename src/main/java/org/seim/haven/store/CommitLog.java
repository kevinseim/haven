package org.seim.haven.store;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.seim.haven.Settings;
import org.seim.haven.models.Token;
import org.seim.haven.response.Response;
import org.seim.haven.util.ByteUtils;
import org.seim.haven.util.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kevin Seim
 */
final class CommitLog {

  private static final Logger log = LoggerFactory.getLogger(CommitLog.class);
  
  public static final byte[] FILE_HEADER = "HAVEN".getBytes(Charsets.ASCII);
  private static final byte[] FILE_VERSION = "v1".getBytes(Charsets.ASCII);
  private static final long ROLL_FILE_LENGTH = Settings.getLong("commitlog.size");
  private static final long COMMIT_INTERVAL = Settings.getLong("commitlog.fsync");
  
  private final Path dir = Paths.get(Settings.getString("commitlog.dir"));
  private final Object lock = new Object();
  
  private FileOutputStream fout;
  private BufferedOutputStream bout;
  // the total file length of commit.log.1
  private long fileLength;
  // the number bytes that have not been flushed to commit.log.1
  private long fileLengthUncommitted;
  private Thread daemon;
  private boolean active = true;
  private List<Segment> segments;
  private long lastRevision = 1;
  
  public CommitLog() { }
  
  /**
   * Initializes the commit log.  Must be called before {@link #start()}.
   * @throws IOException
   */
  public void init() throws IOException {
    loadSegments();
  }
  
  public List<Segment> getSegments() {
    return segments;
  }
  
  private void loadSegments() throws IOException {
    Pattern pattern = Pattern.compile("commit.log.([1-9]+[0-9]*)");
    List<Segment> segments = new ArrayList<>();
    
    if (!Files.isDirectory(dir)) {
      throw new IOException("Commit log directory not found: " + dir);
    }
    try (DirectoryStream<Path> dirList = Files.newDirectoryStream(dir, "commit.log.*")) {
      for (Path path : dirList) {
        Matcher m = pattern.matcher(path.getFileName().toString());
        if (m.matches()) {
          Segment seg = new Segment();
          seg.n = Integer.valueOf(m.group(1));
          seg.path = path;
          segments.add(seg);
        }
      }
    }
    
    if (!segments.isEmpty()) {
      Collections.sort(segments);
      int i = 0;
      for (Segment segment : segments) {
        if (segment.n != ++i) {
          throw new FileNotFoundException("Commit log segment 'commit.log." + i + "' not found");
        }
      }
    }
    
    this.segments = segments;
  }
  
  /**
   * Starts the commit log daemon process.  Must be called before
   * anything can be written to the log.
   * @throws IOException
   */
  public void start() throws IOException {
    synchronized (lock) {
      if (daemon != null) {
        throw new IllegalStateException("Commitlog already started");
      }
      
      open();
      
      daemon = new Daemon();
      daemon.setName("commitlog");
      daemon.setDaemon(true);
      daemon.start();
    }
  }
  
  protected void open() throws IOException {
    if (segments.isEmpty()) {
      createSegment();
    }
    openSegment();
  }
  
  protected void clear() throws IOException {
    for (Segment segment : segments) {
      Files.delete(segment.path);
    }
    segments.clear();
  }
  
  private void createSegment() throws IOException {
    Segment segment = new Segment();
    segment.n = 1;
    segment.path = dir.resolve(Paths.get("commit.log.1"));
    segment.revision = lastRevision;
    segments.add(0, segment);
  }
  
  private void openSegment() throws IOException {
    File file = segments.get(0).path.toFile();
    fileLength = file.exists() ? file.length() : 0;
    fileLengthUncommitted = 0;
    fout = new FileOutputStream(file, true);
    bout = new BufferedOutputStream(fout, 4096);
    if (fileLength == 0) {
      writeFileHeader();
      logRevision(lastRevision);
    }
  }
  
  public void shutdown() throws IOException {
    synchronized (lock) {
      if (daemon == null) {
        return;
      }
      active = false;
      lock.notify();
    }
    
    try {
      daemon.join();
    } catch (InterruptedException e) { 
      // ignored
    } finally {
      daemon = null;
    }
    
    synchronized (lock) {
      close();
    }
  }
  
  public void purgeSegments(long revision) {
    synchronized (lock) {
      boolean purge = false;
      for (Iterator<Segment> iter = segments.iterator(); iter.hasNext(); ) {
        Segment s = iter.next();
        if (purge) {
          try {
            Files.delete(s.path);
            iter.remove();
          } catch (IOException e) {
            log.error("Error purging commit log '" + s.path + "'.  Shutting down.", e);
            System.exit(1);
          }
        } 
        else if (s.revision < revision) {
          purge = true;
        }
      }
    }
  }
  
  private void writeFileHeader() throws IOException {
    bout.write(FILE_HEADER);
    bout.write('\r');
    bout.write('\n');
    bout.write(FILE_VERSION);
    bout.write('\r');
    bout.write('\n');    
    addLength(FILE_HEADER.length + FILE_VERSION.length + 4);
  }
  
  public void logRevision(long revision) {
    synchronized (lock) {
      try {
        byte [] size = Long.toString(revision).getBytes(Charsets.ASCII); 
        bout.write('r');
        bout.write(size);
        bout.write('\r');
        bout.write('\n');
        addLength(3 + size.length);
        this.lastRevision = revision;
      }
      catch (IOException e) {
        log.error("Error writing to commit log.  Shutting down.", e);
        System.exit(1);
      }
    }
  }
  
  public void log(Token[] tokens) {
    synchronized (lock) {
      try {
        byte [] size = Integer.toString(tokens.length).getBytes(Charsets.ASCII); 
        bout.write('*');
        bout.write(size);
        bout.write('\r');
        bout.write('\n');
        fileLengthUncommitted += 3 + size.length;
        for (Token token : tokens) {
          writeToken(token);
        }
        if (fileLength > ROLL_FILE_LENGTH) {
          lock.notify();
        }
      }
      catch (IOException e) {
        log.error("Error writing to commit log.  Shutting down.", e);
        System.exit(1);
      }
    }
  }
  
  private void writeToken(Token token) throws IOException {
    byte[] val = token.value();
    byte[] size = Integer.toString(val.length).getBytes(Response.UTF8);
    
    bout.write('$');
    bout.write(size);
    bout.write('\r');
    bout.write('\n');
    
    bout.write(val);
    bout.write('\r');
    bout.write('\n');
    
    addLength(5 + size.length + val.length);
  }
  
  private void addLength(long length) {
    fileLengthUncommitted += length;
    fileLength += length;
  }
  
  protected void sync() throws IOException {
    // roll commit logs if the current log is full
    if (fileLength > ROLL_FILE_LENGTH) {
      roll();
    } else { // otherwise just sync the file system
      flush();
    }
  }
  
  protected void roll() throws IOException {
    long start = System.currentTimeMillis();
    
    bout.flush();
    bout.close();
    bout = null;
    fout = null;
    
    Path newPath = null;
    int i = segments.size();
    while (i > 0) {
      Segment s = segments.get(i-1);
      if (newPath == null) {
        newPath = s.path.resolveSibling("commit.log." + (i+1));
      }
      Path renamed = Files.move(s.path, newPath, StandardCopyOption.ATOMIC_MOVE);
      newPath = s.path;
      s.path = renamed;
      s.n = (i+1);
      i--;
    }
    
    fileLength = 0;
    createSegment();
    openSegment();
    
    if (log.isDebugEnabled()) {
      long elapsed = System.currentTimeMillis() - start;
      log.debug("Commit log rolled in {}ms, {} bytes", elapsed, 
          ByteUtils.toHumanReadableByteCount(fileLengthUncommitted));
    }
    
    fileLengthUncommitted  = 0;
  }
  
  protected void flush() throws IOException {
    long start = System.currentTimeMillis();
    bout.flush();
    fout.getFD().sync();
    
    if (log.isDebugEnabled()) {
      long elapsed = System.currentTimeMillis() - start;
      log.debug("Commit log sync'd in {}ms, {} bytes", elapsed, 
          ByteUtils.toHumanReadableByteCount(fileLengthUncommitted));
    }
    
    fileLengthUncommitted  = 0;
  }
  
  protected void close() throws IOException {
    long start = System.currentTimeMillis();
    bout.flush();
    bout.close();
    bout = null;
    fout = null;
    
    if (log.isDebugEnabled()) {
      long elapsed = System.currentTimeMillis() - start;
      log.debug("Commit log closed in {}ms, {} bytes", elapsed, 
          ByteUtils.toHumanReadableByteCount(fileLengthUncommitted));
    }
  }
  
  private class Daemon extends Thread {
    public void run() {
      while (true) {
        synchronized (lock) {
          try {
            lock.wait(COMMIT_INTERVAL);
          } catch (InterruptedException e) { }
          
          if (!active) {
            return;
          }
          
          if (fileLengthUncommitted > 0) {
            try {
              sync();
            } catch (IOException e) {
              log.error("Error writing to commit log.  Shutting down.", e);
              System.exit(1);
            }
          }
        }
      }
    }
  }
}
