package org.seim.haven.features

import org.junit.Test;
import org.seim.haven.AbstractServerTest;

/**
 * @author Kevin Seim
 */
class BlobsTest extends AbstractServerTest {

  @Test
  void testSet() {
    assert client.test("set key value1") == "OK"
    assert client.test("expire key 100") == 1
    assert client.test("set key value2") == "OK"
    sleep(100)
    assert client.test("get key") == "value2"
  }
  
  @Test
  void testMget() {
    assert client.test("set key1 value1") == "OK"
    assert client.test("set key2 value2") == "OK"
    assert client.test("mget key1") == [ "value1" ]
    assert client.test("mget key1 key2") == [ "value1", "value2"]
    assert client.test("mget doesnotexist key1 key2") == [ null, "value1", "value2"]
    assert client.test("mget") == "ERR"
  }

  @Test
  void testStrlen() {
    assert client.test("set key1 value1") == "OK"
    assert client.test("strlen key1") == 6
    assert client.test("strlen") == "ERR"
    assert client.test("strlen key1 key2") == "ERR"
  }
  
  @Test
  void testType() {
    assert client.test("set key1 value1") == "OK"
    assert client.test("type key1") == "string"
  }
  
  @Test
  void testRestore() {
    assert client.test("set key value") == "OK"
    
    // test replay from commit log
    store.reload();
    assert client.test("get key") == "value"
    
    // test load from snapshot
    store.snapshot();
    store.clearLogAndReload();
    assert client.test("get key") == "value"
  }
}
