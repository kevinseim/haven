package org.seim.haven.models;

/**
 * @author Kevin Seim
 */
public enum ModelType {
  
  REVISION((byte) 0x00),
  BLOB((byte) 0x01),
  COUNTER((byte) 0x02),
  
  RESERVED_EXPIRES((byte) 0xFD),
  RESERVED_COMPRESSED_BLOCK((byte) 0xFE),
  RESERVED_EOF((byte) 0xFF);
  
  private byte id;
  
  ModelType(int id) {
    this.id = (byte) id;
  }
  
  public byte getId() {
    return id;
  }
  
  public static ModelType fromId(byte id) {
    for (ModelType type : values()) {
      if (type.id == id) {
        return type;
      }
    }
    return null;
  }
}