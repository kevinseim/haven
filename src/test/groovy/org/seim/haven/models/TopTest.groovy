package org.seim.haven.models

import org.junit.Test
import org.seim.haven.models.Top;
import org.xerial.snappy.Snappy

class TopTest {
  
  //@Test
  void testSerialization() {
    
    Top top = new Top(1000);
    
    for (int i=0; i<1000; i++) {
      long count =  (long) (i * Math.random() * 1000);
      top.offer("" + i, count);
    }
    
    byte[] b = top.serialize()
    println "legnth ==> " + b.length
    
    byte[] c = Snappy.compress(b);
    println "legnth ==> " + c.length
  }
}