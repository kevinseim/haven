package org.seim.haven.store

import org.junit.Test
import org.seim.haven.AbstractServerTest

/** 
 * @author Kevin Seim
 */
class RevisionStoreTest extends AbstractServerTest {

  /**
   * Validate revision numbers are incremented appropriately.
   */
  @Test
  void testRevisionIncrements() {
    // a new store should have revision 0
    assert store.getRevision() == 0
    
    // read-only transactions should not increment the revision
    assert client.test("get key1") == null
    
    // the first transaction should increment the revision
    assert client.test("set key1 value") == "OK"
    assert store.getRevision() == 1
    
    // another transaction should not affect the revision
    assert client.test("set key2 value") == "OK"
    assert store.getRevision() == 1
    
    // taking a snapshot should force the revision to increment on the next transaction
    store.snapshot();
    assert store.getRevision() == 1
    assert client.test("set key3 value") == "OK"
    assert store.getRevision() == 2
    
    // further transaction should not increment the revision again
    assert client.test("set key3 value") == "OK"
    assert store.getRevision() == 2
    
    // validate taking a 2nd snapshot restarts the process
    store.snapshot();
    assert client.test("get key3") == "value"
    assert store.getRevision() == 2
    assert client.test("set key4 value") == "OK"
    assert store.getRevision() == 3
  }
}
