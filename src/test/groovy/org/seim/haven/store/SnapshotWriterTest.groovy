package org.seim.haven.store

import org.junit.Test
import org.seim.haven.models.Blob;
import org.seim.haven.models.Token
import org.seim.haven.store.Revision;
import org.seim.haven.store.SnapshotReader;
import org.seim.haven.store.SnapshotWriter;

class SnapshotWriterTest {

  @Test
  void testSnapshotWriter() {
    
    File file = File.createTempFile("snapshot",".hdb");
    file.deleteOnExit();
    
    Token key = new Token("key");
    Blob value = new Blob("value");
    
    Database.clear();
    Database.put(key, value);
    
    Revision rev = new Revision();
    rev.incrementIfWatched();
    assert rev.getValue() == 1
    
    SnapshotWriter out = new SnapshotWriter(file);
    out.write(rev);
    assert out.getStartingRevision() == 1
    
    Database.clear();
    assert !Database.get(key)
    
    System.out.println(file.length() + " bytes");
    
    SnapshotReader reader = new SnapshotReader(file);
    Map revisions = [:]
    assert reader.load(revisions) == 1 
    assert Database.get(key).toString() == "value"
    assert revisions.get(key) == 1
  }
}
