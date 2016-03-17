package org.seim.haven.commands.impl;

import java.util.ArrayList;
import java.util.List;

import org.seim.haven.models.Token;

/**
 * @author Kevin Seim
 */
public final class Option {

  private final String name;
  private final Token token;
  private final boolean hasParameter;
  private final boolean repeating;
  
  public Option(String name) {
    this(name, false, false);
  }
  
  public Option(String name, boolean hasParam) {
    this(name, hasParam, false);
  }
  
  public Option(String name, boolean hasParam, boolean repeats) {
    if (name == null || name.startsWith("-")) {
      throw new IllegalArgumentException("name");
    }
    this.name = name;
    this.token = new Token("-" + name);
    this.hasParameter = hasParam;
    this.repeating = repeats;
  }
  
  public String getName() {
    return name;
  }
  
  public Token getToken() {
    return token;
  }
  
  public boolean hasParameter() {
    return hasParameter;
  }
  
  public boolean isRepeating() {
    return repeating;
  }
  
  public Token getValue(Token[] args, int from, int to) {
    int index = findIndex(args, from, to) + 1;
    if (index > 0 && index < to) {
      return args[index];
    } else {
      return null;
    }
  }
  
  public List<Token> getValues(Token[] args, int from, int to) {
    List<Token> values = null;
    for (int i=from, j=to-1; i<j; i++) {
      if (token == args[i]) {
        if (values == null) {
          values = new ArrayList<>();
        }
        values.add(args[++i]);
      }
    }
    return values;
  }
  
  public boolean isPresent(Token[] args, int from, int to) {
    return findIndex(args, from, to) >= 0;
  }
  
  private int findIndex(Token[] args, int from, int to) {
    for (int i=from; i<to; i++) {
      if (token == args[i]) {
        return i;
      }
    }
    return -1;
  }
}
