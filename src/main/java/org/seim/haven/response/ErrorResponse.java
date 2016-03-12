package org.seim.haven.response;

public class ErrorResponse extends SimpleResponse {

  public ErrorResponse(String value) {
    super(value);
  }

  @Override
  protected byte getIdentifier() {
    return '-';
  }
  
}
