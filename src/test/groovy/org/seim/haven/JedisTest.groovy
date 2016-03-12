package org.seim.haven

import org.junit.Test

import redis.clients.jedis.Jedis

class JedisTest extends AbstractServerTest {

  @Test
  void testClose() {
    Jedis jedis = new Jedis("localhost", 8080);
    try {
      assert jedis.incr("c") == 1
      assert jedis.incr("c") == 2
    } finally {
      jedis.close();
    }
  }
}
