package org.fbi.ltf.server.httpserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * Created by Thinkpad on 2015/10/28.
 */
public class HttpServerInitializer extends ChannelInitializer<SocketChannel>  {
    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpServerHandler());
/*        p.addLast("decoder", new MessageDecoder());
        p.addLast("encoder", new MessageEncoder());
        p.addLast("handler", new HttpServerHandler());*/
        /*p.addLast(new HttpServerCodec());
        p.addLast(new HttpServerHandler());*/
    }
}
