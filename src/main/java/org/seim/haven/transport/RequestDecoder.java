package org.seim.haven.transport;

import java.util.List;

import org.seim.haven.commands.InvalidRequestException;
import org.seim.haven.models.Token;
import org.seim.haven.util.Charsets;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

/**
 * @author Kevin Seim
 */
public class RequestDecoder extends ReplayingDecoder<Void> {
  
  private static final int SLOP_LENGTH = Integer.toString(Integer.MAX_VALUE).length();
  
  private int length = -1;
  private Token[] tokens;
  private byte[] slop = new byte[SLOP_LENGTH];
  
  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
    // read the length of the array
    if (length == -1) {
      readArrayLength(buf);
      if (length < 0) {
        return;
      }
    } 
    
    // read array tokens
    while (length > 0 && readToken(buf)) { }

    // forward the tokens read and reset length to prepare for another request
    out.add(tokens);
    length = -1;
    tokens = null;
  }
  
  @Override
  protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    
  }

  private boolean readToken(ByteBuf buf) {
    if (buf.readByte() != '$') {
      throw new InvalidRequestException("Expected string");
    }
    
    int size = readLength(buf);
    if (buf.readableBytes() < (size + 2)) {
      return false;
    }
    
    byte[] str = new byte[size];
    buf.readBytes(str);
    buf.skipBytes(2); // skip CRLF
    tokens[tokens.length - length] = new Token(str);
    --length;
    checkpoint();
    return true;
  }
  
  private void readArrayLength(ByteBuf buf) {
    byte b = buf.readByte();
    if (b != '*') {
      System.out.println(b);
      throw new InvalidRequestException("Expected array");
    }
  
    length = readLength(buf);
    tokens = new Token[length];
    checkpoint();
  }
  
  private int readLength(ByteBuf buf) {
    int i = 0;
    
    byte b;
    while ((b = buf.readByte()) != '\r') {
      slop[i++] = b;
      if (i > 20) {
        throw new InvalidRequestException("Expected CRLF");
      }
    }
    buf.skipBytes(1); // read the \n
    
    int length = -1;
    try {
      length = Integer.parseInt(new String(slop, 0, i, Charsets.ASCII));
    } catch (NumberFormatException e) { }
    
    if (length <= 0) {
      throw new InvalidRequestException("Invalid length");
    }
    
    return length;
  }
}
