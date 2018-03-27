package org.fbi.ltf.server.httpserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Thinkpad on 2015/10/27.
 */
public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private final int port;

    public HttpServer() {
        this.port = Integer.valueOf((String) ProjectConfigManager.getInstance().getProperty("ltf.server.port"));
    }

    public void run() throws Exception {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        logger.info("HttpServer 服务器开始启动......");

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HttpServerInitializer());

            Channel ch = b.bind(port).sync().channel();
            logger.info("HttpServer 服务器已经启动");
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void stop(){
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new HttpServer().run();
    }
}

