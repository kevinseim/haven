package org.seim.haven.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.seim.haven.commands.Processor;
import org.seim.haven.store.Database;
import org.seim.haven.store.Store;

/**
 * @author Kevin Seim
 */
public class TestStore {

  protected Store store;
  private boolean closed = true;
  
  public TestStore(Processor processor) throws IOException { 
    this.store = new Store(processor);
  }
  
  public Store getStore() {
    return store;
  }
  
  public long getRevision() {
    return Database.revision().getValue();
  }
  
  public void load() throws IOException {
    if (closed) {
      store.load();
      store.getCommitLog().open();
      closed = false;
    }
  }
  
  public void close() throws IOException {
    if (!closed) {
      store.getCommitLog().close();
      closed = true;
    }
  }
  
  /**
   * Deletes all commit log segments and the snapshot if it exists.
   * @throws IOException
   */
  public void clear() throws IOException {
    if (!closed) {
      close();
    }
    clearCommitLog();
    clearSnapshot();
  }
  
  public void clearSnapshot() throws IOException {
    if (Files.exists(Store.SNAPSHOT_PATH)) {
      Files.delete(Store.SNAPSHOT_PATH);
    }
  }
  
  public void clearCommitLog() throws IOException {
    store.getCommitLog().clear();
  }
  
  public void clearLogAndReload() throws IOException {
    _reload(true, false);
  }
  
  public void clearSnapshotAndReload() throws IOException {
    _reload(false, true);
  }
  
  public void reload() throws IOException {
    _reload(false, false);
  }
  
  private void _reload(boolean clearLog, boolean clearSnapshot) throws IOException {
    if (!closed) {
      close();
    }
    if (clearLog) {
      this.clearCommitLog();
    }
    if (clearSnapshot) {
      this.clearSnapshot();
    }
    load();
    closed = false;
  }
  
  public void snapshot() throws IOException {
    ensureOpen();
    store.snapshot();
  }
  
  public void flushCommitLog() throws IOException {
    ensureOpen();
    store.getCommitLog().flush();
  }
  
  public void rollCommitLog() throws IOException {
    ensureOpen();
    store.getCommitLog().roll();
  }
  
  private void ensureOpen() {
    if (closed) {
      throw new IllegalStateException("Store is closed");
    }
  }
}
