package org.seim.haven.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.seim.haven.models.Token;
import org.seim.haven.response.Response;
import org.seim.haven.store.Accessor;
import org.seim.haven.store.Database;

/**
 * @author Kevin Seim
 */
public class Processor implements Accessor {

  private static final FutureTask<Void> POISON = new FutureTask<>(new Runnable() {
    public void run() { } 
  }, null);
  
  private final static Map<Token,Command> commands;
  static {
    Map<Token,Command> m = new HashMap<>();
    for (Command command : Commands.list) {
      m.put(new Token(command.getName()), command);
    }
    commands = Collections.unmodifiableMap(m);
  }

  private final BlockingQueue<FutureTask<?>> queue = new SynchronousQueue<>(true);
  private Daemon daemon;

  public Processor() { }
  
  public void start() {
    daemon = new Daemon();
    daemon.start();
  }
  
  public void shutdown() throws InterruptedException {
    queue.put(POISON);
    daemon.join();
    daemon = null;
  }
  
  public Command parseCommand(Token...tokens) {
    Command command = commands.get(tokens[0].toLowerCase());
    if (command == null) {
      throw new InvalidRequestException("Invalid command");
    }
    return command;
  }
  
  public Request parseRequest(Token...tokens) {
    return parseCommand(tokens).parse(tokens);
  }
  
  public Future<Response> submit(Token... tokens) {
    return submit(parseRequest(tokens));
  }
  
  public <T> Future<T> submit(Callable<T> callable) {
    try {
      FutureTask<T> task = new FutureTask<>(callable);
      queue.offer(task, 30, TimeUnit.SECONDS);
      return task;
    } catch (InterruptedException e) {
      throw new InvalidRequestException("Server busy");
    }
  }
  
  private class Daemon extends Thread {
    
    public Daemon() {
      setName("worker");
    }
    
    @Override
    public void run() {
      long lastExpirationCheck = System.currentTimeMillis();
      while (true) {
        try {
          FutureTask<?> task = queue.poll();
          if (task == null) {
            long sleep = 250 - (System.currentTimeMillis() - lastExpirationCheck);
            if (sleep > 0) {
              task = queue.poll(250, TimeUnit.MILLISECONDS);
            }
          }
          if (task == null) {
            Database.cleanupExpiredKeys();
            lastExpirationCheck = System.currentTimeMillis();
          } else if (task == POISON) {
            break;
          } else {
            task.run();
          }
        }
        catch (InterruptedException e) { }
      }
    }
  }
}
