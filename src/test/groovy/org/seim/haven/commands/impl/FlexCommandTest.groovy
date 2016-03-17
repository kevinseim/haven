package org.seim.haven.commands.impl

import org.junit.Test
import org.seim.haven.commands.InvalidRequestException
import org.seim.haven.models.Token

/**
 * @author Kevin Seim
 */
class FlexCommandTest {

  @Test
  void testParameter() {
    List lines
    FlexRequest req
    Option nx = new Option("nx")
    Option ex = new Option("ex")
    Argument key = new Argument("key", 0)
    Argument value = new Argument("value", 1)
    
    TestFlexCommand cmd = new TestFlexCommand()
    cmd.setOptions([nx, ex] as Option[])
    cmd.setArguments([key, value] as Argument[])
    
    lines = [
      "run key1 value1",
      "run -nx key1 value1",
      "run -nx -ex key1 value1"
    ]
    for (String line : lines) {
      req = cmd.parse(line)
      assert req.getValue(key) as String == "key1"
      assert req.getValue(value) as String == "value1"
    }
  }
  
  @Test(expected=InvalidRequestException.class)
  void testTooManyParameters() {
    Argument key = new Argument("key", 0)
    Argument value = new Argument("value", 1)
    TestFlexCommand cmd = new TestFlexCommand()
    cmd.setArguments([key, value] as Argument[])
    cmd.parse("run key1 value1 value2")
  }
  
  @Test(expected=InvalidRequestException.class)
  void testTooFewParameters() {
    Argument key = new Argument("key", 0)
    Argument value = new Argument("value", 1)
    TestFlexCommand cmd = new TestFlexCommand()
    cmd.setArguments([key, value] as Argument[])
    cmd.parse("run key1")
  }
  
  @Test(expected=IllegalArgumentException.class)
  void testInvalidArgumentOffset1() {
    Argument key = new Argument("key", 0)
    Argument value = new Argument("value", 0)
    TestFlexCommand cmd = new TestFlexCommand()
    cmd.setArguments([key, value] as Argument[])
  }
  
  @Test(expected=IllegalArgumentException.class)
  void testInvalidArgumentOffset2() {
    Argument key = new Argument("key", 0)
    Argument value = new Argument("value", 2)
    TestFlexCommand cmd = new TestFlexCommand()
    cmd.setArguments([key, value] as Argument[])
  }
  
  @Test(expected=IllegalArgumentException.class)
  void testInvalidArgumentOffset3() {
    Argument key = new Argument("key", 1)
    Argument value = new Argument("value", 2)
    TestFlexCommand cmd = new TestFlexCommand()
    cmd.setArguments([key, value] as Argument[])
  }
  
  @Test(expected=IllegalArgumentException.class)
  void testInvalidArgumentOccurs() {
    Argument key = new Argument("key", 0, 0, 1)
    Argument value = new Argument("value", 2)
    TestFlexCommand cmd = new TestFlexCommand()
    cmd.setArguments([key, value] as Argument[])
  }
  
  @Test
  void testRepeatingParameters() {
    List lines
    FlexRequest req
    Option nx = new Option("nx")
    Option ex = new Option("ex")
    Argument term = new Argument("term", 0)
    Argument keys = new Argument("keys", 1, 1, Integer.MAX_VALUE)
    
    TestFlexCommand cmd = new TestFlexCommand()
    cmd.setOptions([nx, ex] as Option[])
    cmd.setArguments([term, keys] as Argument[])
    
    lines = [
      "run term1 key1 key2",
      "run -nx term1 key1 key2",
      "run -nx -ex term1 key1 key2"
    ]
    for (String line : lines) {
      req = cmd.parse(line)
      assert req.getValue(term) as String == "term1"
      List vals = req.getValues(keys) as List
      assert vals.size() == 2
      assert vals[0].toString() == "key1"
      assert vals[1].toString() == "key2"
    }
    
    req = cmd.parse("run term1 key1")
    assert req.getValue(keys) as String == "key1"
    List vals = req.getValues(keys) as List
    assert vals.size() == 1
    assert vals[0].toString() == "key1"
  }
  
  @Test(expected=InvalidRequestException.class)
  void testTooManyRepeatingParameters() {
    Argument key = new Argument("key", 0)
    Argument values = new Argument("values", 1, 2, 3)
    TestFlexCommand cmd = new TestFlexCommand()
    cmd.setArguments([key, values] as Argument[])
    cmd.parse("run key1 value1 value2 value3 value4")
  }
  
  @Test(expected=InvalidRequestException.class)
  void testTooFewRepeatingParameters() {
    Argument key = new Argument("key", 0)
    Argument values = new Argument("values", 1, 2, 3)
    TestFlexCommand cmd = new TestFlexCommand()
    cmd.setArguments([key, values] as Argument[])
    cmd.parse("run key1 value1")
  }
  
  @Test
  void testOptions() {
    List lines
    Option nx = new Option("nx")
    Option ex = new Option("ex")
    Option top = new Option("top", true)
    Option m = new Option("m", true, true)
    
    TestFlexCommand cmd = new TestFlexCommand()
    cmd.setOptions([nx, ex, top, m] as Option[])
    
    FlexRequest req = cmd.parse("run -nx")
    assert req.has(nx);
    assert !req.has(ex);
    
    req = cmd.parse("run -nx -ex")
    assert req.has(nx);
    assert req.has(ex);
    
    req = cmd.parse("run")
    assert !req.has(nx);
    assert !req.has(ex);
    
    lines = [
      "run -top key1 -nx",
      "run -nx -top key1"
    ]
    for (String line : lines) {
      req = cmd.parse(line)
      assert req.has(nx);
      assert req.has(top);
      assert req.getValue(top) as String == "key1"
    }
    
    lines = [
      "run -nx -m one -m two",
      "run -m one -nx -m two",
      "run -m one -m two -nx"
    ]
    for (String line : lines) {
      req = cmd.parse(line)
      assert req.has(nx);
      assert req.has(m);
      Token[] vals = req.getValues(m) as List
      assert vals.length == 2
      assert vals[0] as String == "one"
      assert vals[1] as String == "two"
    }
  }
  
  @Test(expected=InvalidRequestException.class)
  void testInvalidOption() {
    Option nx = new Option("nx")
    TestFlexCommand cmd = new TestFlexCommand()
    cmd.setOptions([nx] as Option[])
    cmd.parse("run -ex")
  }
  
  @Test(expected=InvalidRequestException.class)
  void testInvalidRepeatingOption() {
    Option nx = new Option("a", true)
    TestFlexCommand cmd = new TestFlexCommand()
    cmd.setOptions([nx] as Option[])
    cmd.parse("run -a one -a two")
  }
}
