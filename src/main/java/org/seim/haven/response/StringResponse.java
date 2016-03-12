package org.seim.haven.response;

/**
 * @author Kevin Seim
 */
public class StringResponse extends SimpleResponse {
  
  public StringResponse(String value) {
    super(value);
  }

  @Override
  protected byte getIdentifier() {
    return '+';
  }
}
