package org.seim.haven.util

import org.junit.Test;

class PlaceholderUtilsTest {

  @Test
  void testResolve() {
    System.setProperty("three", "3")
    
    Properties props = new Properties();
    props.setProperty("one", "1");
    props.setProperty("two", "2");
    props.setProperty("three", "ignored");
    
    PlaceholderUtils.resolve("one", props) == "one"
    PlaceholderUtils.resolve("\${one}", props) == "1"
    PlaceholderUtils.resolve("\${three}", props) == "3"
    PlaceholderUtils.resolve("\${one} \${two}", props) == "1 2"
    PlaceholderUtils.resolve("\$one}", props) == "\$one}"
    PlaceholderUtils.resolve("\${one \${two}", props) == "\${one \${two}"
  }
  
}
