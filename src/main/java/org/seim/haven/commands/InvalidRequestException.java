package org.seim.haven.commands;

/**
 * @author Kevin Seim
 */
public class InvalidRequestException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public InvalidRequestException() { }

  public InvalidRequestException(String message) {
    super("ERR: " + message);
  }

  public InvalidRequestException(Throwable cause) {
    super(cause);
  }

  public InvalidRequestException(String message, Throwable cause) {
    super("ERR: " + message, cause);
  }

}
