package org.seim.haven.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 
 * @author Kevin Seim
 * @param <T>
 */
public class FutureImpl<T> implements Future<T> {

  private Callable<T> callable;
  
  public FutureImpl(Callable<T> callable) {
    this.callable = callable;
  }
  
  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return true;
  }

  @Override
  public T get() throws InterruptedException, ExecutionException {
    try {
      return callable.call();
    } catch (Exception e) {
      throw new ExecutionException(e);
    }
  }

  @Override
  public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    return get();
  }
}
