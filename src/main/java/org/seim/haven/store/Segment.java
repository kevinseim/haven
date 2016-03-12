package org.seim.haven.store;

import java.nio.file.Path;

class Segment implements Comparable<Segment> {
  Integer n;
  Path path;
  long revision;
  
  @Override
  public int compareTo(Segment o) {
    return n.compareTo(o.n);
  }
  
  public Path getPath() {
    return path;
  }
  
  public void setHighestRevision(long revision) {
    this.revision = revision;
  }
}