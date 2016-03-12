package org.seim.haven.models;

public abstract class AbstractModel implements Model {

  private Long expirationTime;
  
  @Override
  public Long getExpirationTime() {
    return expirationTime;
  }

  @Override
  public void setExpirationTime(Long expirationTime) {
    this.expirationTime = expirationTime;
  }
  
}
