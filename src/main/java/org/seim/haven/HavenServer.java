package org.seim.haven;

import java.io.IOException;

import org.seim.haven.commands.Processor;
import org.seim.haven.store.Store;
import org.seim.haven.transport.Transport;
import org.seim.haven.util.MemoryMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 * @author Kevin Seim
 */
public final class HavenServer {

  private static final Logger log = LoggerFactory.getLogger(HavenServer.class);
  
  private Store store;
  private Transport transport;
  private Processor processor;
  private MemoryMonitor memoryMonitor;
  
  public HavenServer() { }
  
  /**
   * Starts the Haven server.
   * @throws IOException
   */
  public void start() throws IOException {

    // create a command processor
    processor = new Processor();
    
    // create a store and initialize the database
    store = new Store(processor);
    
    // start the command processor
    processor.start();
    
    // start the store for commit log and snapshot creation
    store.load();
    store.start();
    
    memoryMonitor = new MemoryMonitor();
    memoryMonitor.start();
    
    // create and start a new transport
    transport = new Transport((int)Settings.getLong("server.port"));
    transport.setProcessor(processor);
    try {
      transport.start();
    } catch (InterruptedException e) {
      System.exit(1);
    }
  }
  
  /**
   * Stops the haven server.
   */
  public void shutdown() {
    if (transport != null) {
      try {
        transport.shutdown();
      } catch (InterruptedException e) {
        log.warn("Transport shutdown interrupted", e);
      }
    }
    
    if (store != null) {
      store.shutdown();
    }
    
    if (processor != null) {
      try {
        processor.shutdown();
      } catch (InterruptedException e) {
        log.warn("Haven processor shutdown interrupted", e);
      }
    }
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    final HavenServer server = new HavenServer();
    
    Thread shutdownThread = new Thread() {
      public void run() {
        server.shutdown();
      }
    };
    shutdownThread.setName("shutdown");
    Runtime.getRuntime().addShutdownHook(shutdownThread);
    
    try {
      server.start();
    } catch (Exception e) {
      log.error("Error starting server", e);
      System.exit(1);
    }
  }
}
