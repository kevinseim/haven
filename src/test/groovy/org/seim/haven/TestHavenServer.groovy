package org.seim.haven;

import java.io.IOException;

import org.seim.haven.commands.Processor;
import org.seim.haven.store.TestStore;
import org.seim.haven.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestHavenServer {

  private static final Logger log = LoggerFactory.getLogger(TestHavenServer.class);
  
  private final Transport transport = new Transport(7073);
  private final Processor processor = new Processor();
  private final TestStore store;
  private Thread server;
  
  public TestHavenServer() throws IOException { 
    store = new TestStore(processor);
  }
  
  public TestStore getStore() {
    return store;
  }
  
  public void start() throws IOException, InterruptedException {
    store.clear();
    store.load();
    
    processor.start();
    
    // start the transport in a background thread
    transport.setProcessor(processor);
    server = new Thread() {
      public void run() {
        try {
          transport.start();
        } catch (InterruptedException e) { }
      }
    };
    server.setName("server");
    server.setDaemon(true);
    server.start();
    
    transport.awaitStartup();
  }
  
  public void shutdown() throws IOException, InterruptedException {
    transport.shutdown();
    processor.shutdown();
    store.close();
    server.join();
  }
}
