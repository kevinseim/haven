package org.seim.haven.store;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.seim.haven.Settings;
import org.seim.haven.commands.Processor;
import org.seim.haven.models.Token;
import org.seim.haven.util.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kevin Seim
 */
public final class Store {

  private static final Logger log = LoggerFactory.getLogger(Store.class);
  
  protected static final long SNAPSHOT_INTERVAL = Settings.getLong("snapshot.interval") * 1000L;
  protected static final long SNAPSHOT_TRANSACTION_THRESHOLD = Settings.getLong("snapshot.threshold");
  protected static final Path SNAPSHOT_DIR = Paths.get(Settings.getString("snapshot.dir"));
  protected static final Path SNAPSHOT_PATH = SNAPSHOT_DIR.resolve(Paths.get("snapshot.hdb"));
  protected static final Path SNAPSHOT_PATH_TEMP = SNAPSHOT_DIR.resolve(Paths.get("_snapshot.hdb"));
  
  private final Object lock = new Object();
  
  private Processor processor;
  private CommitLog commitLog;
  // false if shutdown
  private boolean active = true;
  // the number of transactions processed since the last snapshot
  private AtomicLong transactionCount = new AtomicLong();
  private Thread daemon;
  
  public Store(Processor processor) throws IOException { 
    this.processor = processor;
    
    commitLog = new CommitLog();
    commitLog.init();
  }
  
  public void load() throws IOException {
    Database.clear();
    Database.setStore(this);
    Database.setLive(false);
    
    long snapshotRevision = 0;
    ReplayStateImpl state;
    
    // load the most recent snapshot if it exists
    if (Files.exists(SNAPSHOT_PATH)) {
      Map<Token,Long> revisions = new HashMap<>(10000);
      state = new ReplayStateImpl(revisions);
      SnapshotReader reader = new SnapshotReader(SNAPSHOT_PATH.toFile());
      try {
        snapshotRevision = reader.load(revisions);
      } finally {
        reader.close();
      }
    }
    else {
      state = new ReplayStateImpl(Collections.emptyMap());
    }

    // replay commit log segments
    long logRevision = 0;
    long transactionCount = 0;
    List<Segment> segments = new ArrayList<>();
    segments.addAll(commitLog.getSegments());
    Collections.reverse(segments);
    for (Segment segment : segments) {
      log.info("Replaying transactions from '" + segment.path + "'");
      int type;
      try (SegmentReader sr = new SegmentReader(segment)) {
        sr.readHeader();
        while ((type = sr.readNext()) != -1) {
          switch (type) {
          case SegmentReader.REVISION:
            logRevision = sr.getRevision();
            state.setCurrentRevision(logRevision);
            break;
          case SegmentReader.COMMAND:
            if (logRevision > snapshotRevision) {
              Token[] command = sr.getCommand();
              if (processor.parseCommand(command).replay(state, command)) {
                ++transactionCount;
              }
            }
            break;
          }
        }
        segment.setHighestRevision(sr.getRevision());
      }
    }
    long revision = Math.max(logRevision, snapshotRevision);
    Database.setRevision(new Revision(revision));
    this.transactionCount.addAndGet(transactionCount);
    Database.setLive(true);
    
    log.info("Database revision {} loaded, {} keys", revision, Database.size());
  }
  
  public void start() throws IOException {
    commitLog.start();
    
    daemon = new SnapshotDaemon();
    daemon.setName("snapshot");
    daemon.start();
  }
  
  public void shutdown() {
    synchronized (lock) {
      active = false;
      lock.notify();
    }
    
    try {
      commitLog.shutdown();
    } catch (IOException e) {
      log.error("Error closing commit log", e);
    }
    
    try {
      daemon.join();
    } catch (InterruptedException e) {
      log.warn("Snapshot daemon interrupted", e);
    } finally {
      daemon = null;
    }
  }
  
  protected void log(Token... tokens) {
    commitLog.log(tokens);
    transactionCount.incrementAndGet();
  }
  
  protected void log(Revision revision) {
    commitLog.logRevision(revision.getValue());
  }
  
  protected void snapshot() throws IOException {
    long start = System.currentTimeMillis();
    
    File file = SNAPSHOT_PATH_TEMP.toFile();
    SnapshotWriter writer = new SnapshotWriter(file, processor);
    writer.write(Database.revision());

    // replace the last snapshot with the new one
    Files.move(SNAPSHOT_PATH_TEMP, SNAPSHOT_PATH, StandardCopyOption.ATOMIC_MOVE);
    
    if (log.isInfoEnabled()) {
      long elapsed = System.currentTimeMillis() - start;
      log.info("Snapshot created in {}ms, {} keys, {}", elapsed, writer.getKeysWritten(), 
          ByteUtils.toHumanReadableByteCount(writer.getBytesWritten()));
    }
    
    commitLog.purgeSegments(writer.getStartingRevision());
  }
  
  protected CommitLog getCommitLog() {
    return commitLog;
  }
  
  private class SnapshotDaemon extends Thread {
    @Override
    public void run() {
      long lastSnapshot = System.currentTimeMillis();
      
      while (true) {
        synchronized (lock) {
          if (active) {
            long sleep = SNAPSHOT_INTERVAL - (System.currentTimeMillis() - lastSnapshot);
            while (sleep > 0) {
              try {
                lock.wait(sleep);
                if (!active) {
                  return;
                }
                if (transactionCount.get() > SNAPSHOT_TRANSACTION_THRESHOLD) {
                  lastSnapshot = System.currentTimeMillis();
                  break;
                }
              } catch (InterruptedException e) {
                sleep = SNAPSHOT_INTERVAL - (System.currentTimeMillis() - lastSnapshot);
              }
            }
          }
        }
        
        try {
          long delta = transactionCount.get();
          snapshot();
          transactionCount.addAndGet(-delta);
        } catch (IOException e) {
          log.error("Error creating snapshot.  Shutting down", e);
          System.exit(1);
        }
      }
    }
  }
}
