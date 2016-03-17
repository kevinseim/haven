package org.seim.haven.commands.impl;

/**
 * A extended command argument.
 * @author Kevin Seim
 */
public final class Argument implements Comparable<Argument> {

  private final String name;
  private final int offset;
  private final int minOccurs;
  private final int maxOccurs;
  
  public Argument(String name, int offset) {
    this(name, offset, 1, 1);
  }
  
  public Argument(String name, int offset, int minOccurs, int maxOccurs) {
    if (offset < 0) {
      throw new IllegalArgumentException("offset");
    }
    if (minOccurs < 0) {
      throw new IllegalArgumentException("minOccurs");
    }
    if (maxOccurs < 0 || maxOccurs < minOccurs) {
      throw new IllegalArgumentException("maxOccurs");
    }
    
    this.name = name;
    this.offset = offset;
    this.minOccurs = minOccurs;
    this.maxOccurs = maxOccurs;
  }

  public String getName() {
    return name;
  }

  public int getMinOccurs() {
    return minOccurs;
  }

  public Integer getMaxOccurs() {
    return maxOccurs;
  }
  
  public int getOffset() {
    return offset;
  }

  @Override
  public int compareTo(Argument o) {
    return Integer.valueOf(this.offset).compareTo(Integer.valueOf(o.offset));
  }
}
