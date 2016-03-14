package org.seim.haven.store

import org.junit.Test
import org.seim.haven.models.Blob
import org.seim.haven.models.Token
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SnapshotWriterTest {

  private static final Logger log = LoggerFactory.getLogger(SnapshotWriterTest.class);
  
  @Test
  void testSnapshotWriter() {
    
    int bufferSize = 256 + Math.random() * 4096;
    log.info("Using random buffer size of {}", bufferSize);
    
    Database.clear();
    
    File file = File.createTempFile("snapshot",".hdb");
    file.deleteOnExit();
    
    Token key = new Token("key");
    Blob value = new Blob("value");
    Database.put(key, value);
    
    for (int i=0; i<10000; i++) {
      Database.put(new Token("key" + i), new Blob("value" + i));
    }
    
    Revision rev = new Revision();
    rev.incrementIfWatched();
    assert rev.getValue() == 1
    
    SnapshotWriter out = new SnapshotWriter(file, bufferSize);
    out.write(rev);
    assert out.getStartingRevision() == 1
    
    Database.clear();
    assert !Database.get(key)
    
    SnapshotReader reader = new SnapshotReader(file, bufferSize);
    Map revisions = [:]
    assert reader.load(revisions) == 1 
    assert Database.get(key).toString() == "value"
    assert Database.get(new Token("key255")).toString() == "value255"
    assert revisions.get(key) == 1
  }
}
