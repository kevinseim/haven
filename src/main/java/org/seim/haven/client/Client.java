package org.seim.haven.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.seim.haven.response.ArrayResponse;
import org.seim.haven.response.BlobResponse;
import org.seim.haven.response.ErrorResponse;
import org.seim.haven.response.IntegerResponse;
import org.seim.haven.response.NilResponse;
import org.seim.haven.response.Response;
import org.seim.haven.response.StringResponse;
import org.seim.haven.util.Charsets;
import org.seim.haven.util.IOUtils;

/**
 * A simple Haven client.
 * @author Kevin Seim
 */
public class Client {

  private String host;
  private int port;
  
  private Socket socket;
  private BufferedInputStream in;
  private PrintWriter out;
  private ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);
  
  /**
   * Creates a new client for connecting to localhost:8080.
   */
  public Client() {
    this("localhost", 8080);
  }
  
  public Client(String host, int port) {
    this.host = host;
    this.port = port;
  }
  
  public void start() throws IOException {
    connect();
  }
  
  public void shutdown() throws IOException {
    disconnect();
  }
  
  private void connect() throws IOException {
    socket = new Socket(host, port);
    socket.setKeepAlive(true);
    socket.setSoTimeout(30000);
    in = new BufferedInputStream(socket.getInputStream());
    out = new PrintWriter(socket.getOutputStream(), false);
  }
  
  private void disconnect() {
    IOUtils.closeQuietly(out);
    IOUtils.closeQuietly(in);
    try {
      if (socket != null) {
        socket.close();
      }
    } catch (IOException e) {  }
    out = null;
    in = null;
    socket = null;
  }
  
  private boolean isConnected() throws IOException {
    return socket != null && socket.isBound() && !socket.isClosed()
        && socket.isConnected() && !socket.isInputShutdown()
        && !socket.isOutputShutdown();
  }
  
  public Object test(String command) throws IOException {
    return toObject(send(command));
  } 
  
  private Object toObject(Response response) {
    if (response instanceof ArrayResponse) {
      List<Object> list = new ArrayList<>();
      for (Response r : ((ArrayResponse)response).getResponses()) {
        list.add(toObject(r));
      }
      return list;
    } else if (response instanceof NilResponse) {
      return null;
    } else if (response instanceof IntegerResponse) {
      return Charsets.toLong(((IntegerResponse)response).value());
    } else if (response instanceof ErrorResponse) {
      System.out.println(response.toString());
      return "ERR";
    } else {
      return response.toString();
    }
  }
  
  public Response send(String command) throws IOException {
    if (!isConnected()) {
      disconnect();
      connect();
    }
    String[] args = command.split(" ");
    out.write("*" + args.length + "\r\n");
    for (int i=0; i<args.length; i++) {
      out.write("$" + args[i].length() + "\r\n");
      out.write(args[i]);
      out.write("\r\n");
    }
    out.flush();
    return readResponse();
  }
  
  private Response readResponse() throws IOException {
    int n = in.read();
    switch (n) {
    case '*':
      return readArrayResponse();
    case '+':
      return readStringResponse();
    case '-':
      return readErrorResponse();
    case ':':
      return readIntegerResponse();
    case '$':
      return readBlobResponse();
    default:
      throw new IOException("Unknown response: " + n);
    }
  }
  
  private Response readBlobResponse() throws IOException {
    int size = (int) Charsets.toLong(readLine());
    if (size == -1) {
      return Response.NIL;
    }
    byte[] value = new byte[size];
    if (in.read(value) != size) {
      throw new EOFException();
    }
    if (in.read() != '\r' || in.read() != '\n') {
      throw new EOFException();
    }
    return new BlobResponse(value);
  }
  
  private ArrayResponse readArrayResponse() throws IOException {
    int size = (int) Charsets.toLong(readLine());
    Response[] rs = new Response[size];
    for (int i=0; i<size; i++) {
      rs[i] = readResponse();
    }
    return new ArrayResponse(rs);
  }
  
  private StringResponse readStringResponse() throws IOException {
    return new StringResponse(readString());
  }
  
  private ErrorResponse readErrorResponse() throws IOException {
    return new ErrorResponse(readString());
  }
  
  private IntegerResponse readIntegerResponse() throws IOException {
    return new IntegerResponse(Long.valueOf(readString()));
  }
  
  private String readString() throws IOException {
    return new String(readLine(), Charsets.UTF8);
  }
  
  private byte[] readLine() throws IOException {
    while (true) {
      int n = in.read();
      if (n == -1) {
        throw new EOFException();
      } else if (n == '\r') {
        break;
      } else {
        buffer.write((byte)n);
      }
    }
    if (in.read() != '\n') {
      throw new EOFException();
    }
    
    byte[] val = buffer.toByteArray();
    buffer.reset();
    return val;
  }
}
