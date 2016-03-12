package org.seim.haven.models;

import java.nio.ByteBuffer;

public class Blob extends AbstractModel {

  private final Token token;
  
  public Blob() { 
    token = new Token();
  }
  
  public Blob(String value) {
    token = new Token(value);
  }
  
  public Blob(byte[] value) {
    token = new Token(value);
  }
  
  public Blob(Token token) {
    this.token = token;
  }
  
  @Override
  public ModelType type() {
    return ModelType.BLOB;
  }

  @Override
  public int deserialize(ByteBuffer buf, int version) {
    return token.deserialize(buf, version);
  }

  @Override
  public void serialize(ByteBuffer buf) {
    token.serialize(buf);
  }

  @Override
  public int getSerializedLength() {
    return token.getSerializedLength();
  }
  
  public Token getToken() {
    return token;
  }
  
  @Override
  public String toString() {
    return token.toString();
  }
}
