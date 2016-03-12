package org.seim.haven.store;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.seim.haven.models.Token;

/**
 * @author Kevin Seim
 */
class SegmentReader implements Closeable {

  public static final int REVISION = 0;
  public static final int COMMAND= 1;
  
  private FileInputStream fin;
  private BufferedInputStream bin;
  
  private byte[] newline = new byte[2];
  private int version;
  
  private transient long revision;
  private transient Token[] command;
  
  public SegmentReader(Segment segment) throws IOException {
    fin = new FileInputStream(segment.getPath().toFile());
    bin = new BufferedInputStream(fin);
  }
  
  public SegmentReader(InputStream in) throws IOException {
    if (in instanceof BufferedInputStream) {
      bin = (BufferedInputStream) in;
    } else {
      bin = new BufferedInputStream(in);
    }
  }
  
  @Override
  public void close() throws IOException {
    bin.close();
    bin = null;
    fin = null;
  }
  
  public long getRevision() {
    return revision;
  }
  
  public Token[] getCommand() {
    return command;
  }
  
  public Token[] readCommand() throws IOException {
    int n = readNext();
    if (n < 0) {
      return null;
    }
    if (n == REVISION) {
      return readCommand();
    } else {
      return command;
    }
  }
  
  public int readNext() throws IOException {
    int type = bin.read();
    if (type == -1) {
      return -1;
    }
    
    switch (type) {
    case 'r':
      this.revision = readNumber();
      return REVISION;
      
    case '*':
      int size = (int) readNumber();
      Token[] tokens = new Token[size];
      for (int i=0; i<size; i++) {
        tokens[i] = readToken();
      }
      this.command = tokens;
      return COMMAND;
      
    default:
      throw new IOException("Invalid record identifier: " + (char)type);
    }
  }
  
  private Token readToken() throws IOException {
    if (bin.read() != '$') {
      throw new IOException("Expected token");
    }
    Token token = new Token(read((int) readNumber()));
    readNewLine();
    return token;
  }
  
  private byte[] read(int length) throws IOException {
    byte[] b = new byte[length];
    int i = 0;
    while ((i = bin.read(b, i, length - i)) < length) {
      if (i == -1) {
        throw new EOFException();
      }
    }
    return b;
  }
  
  public int readHeader() throws IOException {
    byte[] header = read(CommitLog.FILE_HEADER.length);
    if (!Arrays.equals(header, CommitLog.FILE_HEADER)) {
      throw new IOException("Invalid file header");
    }
    readNewLine();
    if (bin.read() != 'v') {
      throw new IOException("Expected file version");
    }
    this.version = (int) readNumber();
    if (version != 1) {
      throw new IOException("Invalid file version");
    }
    return version;
  }
  
  private long readNumber() throws IOException {
    StringBuilder b = new StringBuilder();
    while (true) {
      int n = bin.read();
      if (n == -1) {
        throw new EOFException();
      } else if (n == '\r') {
        break;
      } else {
        b.append((char)n);
      }
    }
    long n;
    try {
      n = Long.parseLong(b.toString());
    } catch (NumberFormatException e) { 
      throw new IOException("Invalid number");
    }
    if (bin.read() != '\n') {
      throw new IOException("Invalid delimeter");
    }
    return n;
  }
  
  private void readNewLine() throws IOException {
    if (bin.read(newline) != 2) {
      throw new EOFException();
    }
    if (newline[0] != '\r' || newline[1] != '\n') {
      throw new IOException("Invalid delimeter");
    }
  }
}
