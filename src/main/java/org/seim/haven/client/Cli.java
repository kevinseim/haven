package org.seim.haven.client;

import java.io.Console;
import java.io.IOException;
import java.util.List;

import org.seim.haven.response.ArrayResponse;
import org.seim.haven.response.BlobResponse;
import org.seim.haven.response.ErrorResponse;
import org.seim.haven.response.IntegerResponse;
import org.seim.haven.response.NilResponse;
import org.seim.haven.response.Response;
import org.seim.haven.response.StringResponse;
import org.seim.haven.util.IOUtils;

/**
 * Haven command line interface.
 * @author Kevin Seim
 */
public class Cli {

  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 7072;
  
  private final Client client;
  
  protected Cli(Client client) {
    this.client = client;
  }
  
  protected void run() throws IOException {
    Console console = System.console();
    if (console == null) {
      throw new IOException("Cannot read from stdin");
    }
    
    console.printf("Connected to %s:%d%n", client.getHost(), client.getPort());
    
    while (true) {
      String cmd = console.readLine("haven> ");
      if (cmd.trim().length() == 0) {
        continue;
      }
      if ("exit".equals(cmd)) {
        break;
      }
      
      Response resp = client.send(cmd);
      if (resp instanceof ArrayResponse) {
        List<Response> rs = ((ArrayResponse)resp).getResponses();
        for (int i=0; i<rs.size(); i++) {
          console.printf("%d) %s%n", i, format(rs.get(i)));
        }
      } 
      else {
        console.printf("%s%n", format(resp));
      }
    }
  }
  
  protected String format(Response resp) {
    if (resp instanceof NilResponse) {
      return "(nil)";
    } else if (resp instanceof StringResponse) {
      return resp.toString();
    } else if (resp instanceof ErrorResponse) {
      return "ERR: " + resp.toString();
    } else if (resp instanceof IntegerResponse) {
      return "(integer) " + resp.toString();
    } else if (resp instanceof BlobResponse) {
      return format((BlobResponse)resp);
    } else {
      return resp.toString();
    }
  }
  
  protected String format(BlobResponse resp) {
    return String.format("\"%s\"", resp.toString());
  }
  
  public static void main(String[] args) {
    String host = args.length > 0 ? args[0] : DEFAULT_HOST;
    int port;
    try {
      port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
    } catch (NumberFormatException e) {
      System.err.println("Invalid port");
      System.exit(1);
      return;
    }
    
    int rc = 0;
    Client client = new Client(host, port);
    try {
      client.connect();
      
      new Cli(client).run();
      
      client.close();
      client = null;
    }
    catch (Exception e) {
      e.printStackTrace();
      rc = 1;
    } 
    finally {
      IOUtils.closeQuietly(client);
    }
    
    System.exit(rc);
  }
}
