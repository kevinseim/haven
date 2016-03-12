package org.seim.haven.transport;

import org.seim.haven.commands.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author Kevin Seim
 */
public class Transport {

  private static final Logger log = LoggerFactory.getLogger(Transport.class);
  
  private final int port;
  private ChannelFuture f;
  private Processor processor;
  
  private final Object lock = new Object();
  private boolean started = false;
  
  public Transport(int port) {
    this.port = port;
  }
  
  public void start() throws InterruptedException {
    EventLoopGroup bossGroup = new NioEventLoopGroup(); 
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
         .channel(NioServerSocketChannel.class)
         .childHandler(new ChannelInitializer<SocketChannel>() {
             @Override
             public void initChannel(SocketChannel ch) throws Exception {
                 ch.pipeline().addLast(
                     new RequestDecoder(),
                     new RequestHandler(processor),
                     new ResponseEncoder());
             }
         })
         .option(ChannelOption.SO_BACKLOG, 128)
         .childOption(ChannelOption.SO_KEEPALIVE, true);

        // Bind and start to accept incoming connections
        f = b.bind(port).sync();
        
        // notify 
        synchronized (lock) {
          started = true;
          lock.notifyAll();
        }
        
        // Wait until the server socket is closed
        log.info("Server started on port {}", port);
        f.channel().closeFuture().sync();
    } 
    finally {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
  }
  
  public void awaitStartup() throws InterruptedException {
    synchronized (lock) {
      if (!started) {
        lock.wait();
      }
    }
  }
  
  public void shutdown() throws InterruptedException {
    if (f != null) {
      f.channel().close().sync();
    }
  }

  public void setProcessor(Processor processor) {
    this.processor = processor;
  }
}
