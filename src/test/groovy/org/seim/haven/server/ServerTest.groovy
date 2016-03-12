package org.seim.haven.server

import org.junit.Test
import org.seim.haven.HavenServer;
import org.seim.haven.client.Client
import org.seim.haven.models.Token
import org.seim.haven.store.Database;
import org.seim.haven.commands.Processor

class ServerTest {

  //@Test
  void testServer() {
    final HavenServer server = new HavenServer();
    Thread t = new Thread() {
      public void run() {
        server.start();
      }
    };
    t.setName("server");
    t.start();
    
    try {
      Thread.sleep(5000);
      
      Client client = new Client();
      client.start();
      
      println client.send('set key val1');
      println client.send('set key val2');
      println client.send("flushall");
      
      /*
      long start = System.currentTimeMillis();
      for (int i=0; i<1_000_000; i++) {
        client.send("incr key" + i);
        //client.send("set key" + i + " value");
      }
      long elapsed = System.currentTimeMillis() - start;
      println elapsed + "ms";
      */
      
      Thread.sleep(30000);
      
      client.shutdown();
    } 
    catch (Exception e) {
      e.printStackTrace();
    }
    finally { 
      server.shutdown();
      t.join();
    }
  }
}
