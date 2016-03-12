package org.seim.haven.response;

/**
 * @author Kevin Seim
 */
public class IntegerResponse extends SimpleResponse {

  public IntegerResponse(long value) {
    super(Long.toString(value).getBytes(UTF8));
  }

  @Override
  protected byte getIdentifier() {
    return ':';
  }
  
}
