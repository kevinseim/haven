package org.seim.haven.features

import org.junit.Test;
import org.seim.haven.AbstractServerTest;

class CounterTest extends AbstractServerTest {

  @Test
  void testIncr() {
    assert client.test("set n 1") == "OK"
    assert client.test("get n") == "1"
    assert client.test("incr n") == 2
    assert client.test("incr n") == 3
    assert client.test("get n") == "3"
    
    assert client.test("set n xxx") == "OK"
    assert client.test("incr n") == "ERR"
  }
  
  @Test
  void testIncrBy() {
    assert client.test("set n 1") == "OK"
    assert client.test("get n") == "1"
    assert client.test("incrby n 2") == 3
    assert client.test("incrby n -8") == -5
    assert client.test("get n") == "-5"
    
    assert client.test("set n xxx") == "OK"
    assert client.test("incrby n 1") == "ERR"
  }
  
  @Test
  void testDecr() {
    assert client.test("set n 1") == "OK"
    assert client.test("get n") == "1"
    assert client.test("decr n") == 0
    assert client.test("decr n") == -1
    assert client.test("get n") == "-1"
    
    assert client.test("set n xxx") == "OK"
    assert client.test("decr n") == "ERR"
  }
  
  @Test
  void testDecrBy() {
    assert client.test("set n 5") == "OK"
    assert client.test("get n") == "5"
    assert client.test("decrby n 2") == 3
    assert client.test("decrby n 3") == 0
    assert client.test("get n") == "0"
    
    assert client.test("set n xxx") == "OK"
    assert client.test("decrby n 1") == "ERR"
  }
  
  @Test
  void testRestoreFromSnapshot() {
    testRestore(true);
  }
  
  @Test
  void testRestoreFromCommitLog() {
    testRestore(false);
  }
  
  private void testRestore(boolean snapshot) {
    assert client.test("flushall") == "OK"
    assert client.test("incr key") == 1
   
    // reload from snapshot only
    if (snapshot) {
      store.snapshot();
      store.clearLogAndReload();
    } else {
      store.clearSnapshotAndReload();
    }
    
    assert client.test("get key") == "1"
  }
}
