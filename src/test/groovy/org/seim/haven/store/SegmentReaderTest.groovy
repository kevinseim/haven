package org.seim.haven.store

import org.junit.Test
import org.seim.haven.models.Token
import org.seim.haven.store.SegmentReader;

/**
 * @author Kevin Seim
 */
class SegmentReaderTest {

  private byte[] header = StoreTestUtils.toCommitLogMessage(
    "HAVEN", "v1")
  
  private byte[] tokenMessage = StoreTestUtils.toCommitLogMessage(
    "*3", "\$3", "set", "\$3", "key", "\$5", "value")

  private byte[] tokenMessageWithRevision = StoreTestUtils.toCommitLogMessage(
    "r123456", "*3", "\$3", "set", "\$3", "key", "\$5", "value")
    
  @Test
  void testReadHeader() {
    assert new SegmentReader(new ByteArrayInputStream(header)).readHeader() == 1
  }
  
  @Test
  void testReadHeaderUnexpectedEOF() {\
    byte[] msg = header;
    for (int i=1; i<msg.length; i++) {
      try {
        SegmentReader sr = new SegmentReader(new ByteArrayInputStream(msg, 0, msg.length - i))
        sr.readHeader()
        assert false, "Expected IOException for message " + new String(msg, 0, msg.length - i)
      } catch (IOException e) {
        // pass
      }
    }
  }
  
  @Test(expected=IOException.class)
  void testReadHeaderInvalidLabel() {
    new SegmentReader(StoreTestUtils.toCommitLogStream("HAVET", "v1")).readHeader()
  }
  
  @Test(expected=IOException.class)
  void testReadHeaderInvalidVersion() {
    new SegmentReader(StoreTestUtils.toCommitLogStream("HAVEN", "v0")).readHeader()
  }
  
  @Test
  void testReadCommand() {
    SegmentReader sr = new SegmentReader(new ByteArrayInputStream(tokenMessage))
    Token[] tokens = sr.readCommand();
    assert tokens.length == 3
    assert tokens[0] == new Token("set")
    assert tokens[1] == new Token("key")
    assert tokens[2] == new Token("value")
  }
  
  @Test
  void testReadCommandWithRevision() {
    SegmentReader sr = new SegmentReader(new ByteArrayInputStream(tokenMessageWithRevision))
    Token[] tokens = sr.readCommand();
    assert tokens.length == 3
    assert tokens[0] == new Token("set")
    assert tokens[1] == new Token("key")
    assert tokens[2] == new Token("value")
    assert sr.getRevision() == 123456L
  }
  
  @Test
  void testReadCommandEOF() {
    SegmentReader sr = new SegmentReader(new ByteArrayInputStream(new byte[0]))
    assert sr.readCommand() == null
  }
  
  @Test
  void testReadCommandUnexpectedEOF() {
    byte[] msg = tokenMessageWithRevision;
    for (int i=1; i<msg.length; i++) {
      // just a revision is allowed here
      if (i == 33) {
        continue;
      }
      try {
        SegmentReader sr = new SegmentReader(new ByteArrayInputStream(msg, 0, msg.length - i))
        sr.readCommand()
        assert false, "Expected IOException for message " + new String(msg, 0, msg.length - i)
      } catch (IOException e) {
        // pass
      }
    }
  }
}
