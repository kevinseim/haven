package org.seim.haven.features

import org.junit.Test

import org.seim.haven.AbstractServerTest

/**
 * @author Kevin Seim
 */
class ExpirationTest extends AbstractServerTest {

  @Test
  void testExpires() {
    assert client.test("set key value") == "OK"
    assert client.test("expire key 500") == 1
    assert client.test("get key") == "value"
    sleep(500)
    assert client.test("get key") == null
  }
  
  @Test
  void testPersist() {
    assert client.test("set key value") == "OK"
    assert client.test("expire key 500") == 1
    assert client.test("persist key") == 1
    sleep(500)
    assert client.test("get key") == "value"
  }
  
  @Test
  void testExpireAt() {
    assert client.test("set key value") == "OK"
    long at = System.currentTimeMillis() + 250;
    assert client.test("expireat key " + at) == 1
    assert client.test("get key") == "value"
    long toe = client.test("toe key")
    assert toe > (at - 50) && toe < (at + 50) 
    sleep(500)
    assert client.test("get key") == null
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
    long ttl = 500
    assert client.test("set key value") == "OK"
    assert client.test("expire key " + ttl) == 1
   
    // reload from snapshot only
    long time = System.currentTimeMillis();
    if (snapshot) {
      store.snapshot();
      store.clearLogAndReload();
    }
    else {
      store.clearSnapshotAndReload();
    }
    assert client.test("get key") == "value"
    
    long wait = ttl - (System.currentTimeMillis() - time);
    if (wait > 0) {
      sleep(wait);
    }
    assert client.test("get key") == null
  }
}
