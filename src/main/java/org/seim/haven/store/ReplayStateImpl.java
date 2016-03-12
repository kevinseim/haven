package org.seim.haven.store;

import java.util.Map;

import org.seim.haven.models.Token;

/**
 * @author Kevin Seim
 */
public final class ReplayStateImpl implements ReplayState {

  private final Map<Token,Long> snapshotRevisions;
  private long currentRevision;
  
  ReplayStateImpl(Map<Token,Long> snapshotRevisions) {
    this.snapshotRevisions = snapshotRevisions;
  }
  
  void setCurrentRevision(long revision) {
    this.currentRevision = revision;
  }
  
  public boolean shouldReplay(Token key) {
    Long snapshotRevision = snapshotRevisions.get(key);
    return snapshotRevision == null || snapshotRevision < currentRevision;
  }
}
