package org.seim.haven

import org.junit.AfterClass
import org.junit.BeforeClass
import org.seim.haven.client.Client
import org.seim.haven.store.TestStore

abstract class AbstractServerTest {
  
  protected static TestHavenServer server;
  protected static TestStore store;
  protected static Client client;
  
  @BeforeClass
  static void startServer() {
    server = new TestHavenServer();
    store = server.getStore();
    server.start();
    client = new Client("localhost", 7073);
    client.connect();
  }
  
  @AfterClass
  static void stopServer() {
    client.close();
    client = null;
    server.shutdown();
    server = null;
    store = null;
  }
}
