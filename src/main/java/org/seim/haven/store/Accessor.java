package org.seim.haven.store;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface Accessor {

  public <T> Future<T> submit(Callable<T> callable) throws InterruptedException, ExecutionException;
  
}
