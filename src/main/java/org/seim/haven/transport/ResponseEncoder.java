package org.seim.haven.transport;

import org.seim.haven.response.Response;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Kevin Seim
 */
public class ResponseEncoder extends ChannelInboundHandlerAdapter {

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    Response response = (Response) msg;
    ByteBuf buf = ctx.alloc().buffer(response.getSerializedLength());
    response.serialize(buf);
    ctx.writeAndFlush(buf);
  }
  
}
