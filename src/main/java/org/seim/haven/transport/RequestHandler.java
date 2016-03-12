package org.seim.haven.transport;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.seim.haven.commands.Command;
import org.seim.haven.commands.Commands;
import org.seim.haven.commands.InvalidRequestException;
import org.seim.haven.commands.Processor;
import org.seim.haven.commands.Request;
import org.seim.haven.commands.transactions.MultiRequest;
import org.seim.haven.models.Token;
import org.seim.haven.response.ErrorResponse;
import org.seim.haven.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

/**
 * @author Kevin Seim
 */
public class RequestHandler extends MessageToMessageDecoder<Token[]> {

  private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
  
  private Processor processor;
  private MultiRequest multi;
  
  public RequestHandler(Processor processor) {
    this.processor = processor;
  }
  
  @Override
  protected void decode(ChannelHandlerContext ctx, Token[] tokens, List<Object> out) throws Exception {
    try {
      Command command = processor.parseCommand(tokens);
      Request request;

      // start a transaction?
      if (command == Commands.MULTI) {
        if (multi != null) {
          throw new InvalidRequestException("Transaction already started");
        }
        multi = (MultiRequest) command.parse(tokens);
        return;
      }
      // abort a transaction?
      else if (command == Commands.DISCARD) {
        this.multi = null;
        out.add(Response.OK);
        return;
      }
      // execute a transaction?
      else if (command == Commands.EXEC) {
        if (multi == null) {
          throw new InvalidRequestException("Transaction not started with 'multi'");
        }
        request = multi;
      }
      else {
        request = command.parse(tokens);
        if (command.isProcessed() && multi != null) {
          multi.add(request);
          out.add(Response.QUEUED);
          return;
        }
      }
      
      try {
        Response response;
        if (command.isProcessed()) {
          response = processor.submit(request).get();
        } else {
          response = request.call();
        }
        out.add(response);
      } catch (ExecutionException e) {
        throw (Exception) e.getCause();
      } finally {
        multi = null;
      }
    }
    catch (InvalidRequestException e) {
      out.add(new ErrorResponse(e.getMessage()));
    }
    catch (Exception e) {
      log.error("Internal server error", e);
      out.add(new ErrorResponse("Internal server error"));
    }
  }
  
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (cause instanceof IOException) {
      return;
    }
    log.error("Internal server error", cause);
    ctx.close();
  }
}
