package org.seim.haven.models

import java.nio.ByteBuffer

import org.junit.Test
import org.seim.haven.models.Token;

class TokenTest {

  @Test
  void testEncoding() {
    Token t = new Token("test");
    assert "test".equals(new String(t.value(), "UTF-8"))
  }

  @Test
  void testEquals() {
    Token t1 = new Token("test")
    Token t2 = new Token("test")
    assert t1.hashCode() == t2.hashCode()
    assert t1 == t2
    assert t1 != new Token("other")
  }
  
  @Test
  void testLowercase() {
    Token incr = new Token("INCR");
    assert incr.toLowerCase().toString() == "incr";
  }
    
  @Test
  void testLength63() {
    ByteBuffer buf = ByteBuffer.allocate(16);
    
    Token t = new Token("test");
    t.serialize(buf)
    buf.flip()
    
    Token got = new Token()
    got.deserialize(buf, 1)
    assert t == got
  }
}
