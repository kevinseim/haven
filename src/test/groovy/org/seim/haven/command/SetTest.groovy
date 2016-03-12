package org.seim.haven.command

import org.junit.Test
import org.seim.haven.AbstractServerTest

class SetTest extends AbstractServerTest {
  
  @Test
  void testSnapshotRestored() {
    assert client.test("set key value") == "OK"
    
    // test replay from commit log
    store.reload();
    assert client.test("get key") == "value"
    
    long start = System.currentTimeMillis();
    for (int i=1; i<100; i++) {
      client.send("set key" + i + " value");
    }
    long elapsed = System.currentTimeMillis() - start;
    println elapsed + "ms";
    assert client.test("get key") == "value"
    
    // test load from snapshot
    store.snapshot();
    store.clearLogAndReload();
    assert client.test("get key") == "value"
  }
  
}
