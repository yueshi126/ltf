package org.fbi.ltf.server.httpserver;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import org.apache.commons.lang.StringUtils;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;

/**
 * Created by Thinkpad on 2015/10/28.
 */
public class HttpServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);
    private boolean readingChunks;
    private HttpRequest request;
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private HttpPostRequestDecoder decoder;
    private TxnContext txnContext = new TxnContext();
    private Map<String, List<String>> params = new HashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String uri = "";
        String[] names = null;
        String clientPath = "";
        Class className=null;
        String method = "";
        try{
            if (msg instanceof HttpRequest) {
                request = (HttpRequest) msg;
                method = request.getMethod().toString();
                if ("GET".equalsIgnoreCase(method)) {
                    uri = request.getUri();
                    if (!StringUtils.isEmpty(uri)){
                        logger.info("收到对方URI为："+uri);
                        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
                        params = queryStringDecoder.parameters();
                        clientPath = queryStringDecoder.path();
                        names = clientPath.split("\\/");
                        className = Class.forName("org.fbi.ltf.server." + names[1].toLowerCase() + "." + names[2].toUpperCase() + "Processor");
                        txnContext.setMapTia(params);
                        Processor processor = (Processor)className.newInstance();
                        processor.service(txnContext);
                        writeResponse(ctx,uri);
                    }
                }else {
                    try {
                        decoder = new HttpPostRequestDecoder(factory, request);
                    } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                        logger.error("解析POS数据失败");
                        e1.printStackTrace();
                        ctx.channel().close();
                        return;
                    } catch (HttpPostRequestDecoder.IncompatibleDataDecoderException e1) {
                        return;
                    }
                }
                if (HttpHeaders.isTransferEncodingChunked(request)) {
                    readingChunks = true;
                }
            }
            if (decoder != null) {
                if (msg instanceof HttpContent) {
                    // New chunk is received
                    HttpContent chunk = (HttpContent) msg;
                    try {
                        decoder.offer(chunk);
                    } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                        e1.printStackTrace();
                        ctx.channel().close();
                        return;
                    }
                    readHttpDataChunkByChunk();
                    if (chunk instanceof LastHttpContent) {
                        readingChunks = false;
                    }
                }
                uri = request.getUri();
                if ((!StringUtils.isEmpty(uri))&&(params.size()>0)){
                    logger.info("HttpServer收到对方URI为："+uri);
                    clientPath = uri;
                    names = clientPath.split("\\/");
                    className = Class.forName("org.fbi.ltf.server." + names[1].toLowerCase() + "." + names[2].toUpperCase() + "Processor");
                    txnContext.setMapTia(params);
                    Processor processor = (Processor)className.newInstance();
                    processor.service(txnContext);
                    writeResponse(ctx,uri);
                }
            }
        }catch(Exception e){

            logger.error("Processor instance error!", e);
        }
    }

    /**
     * Example of reading request by chunk and getting values from chunk to chunk
     */
    private void readHttpDataChunkByChunk() {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    try {
                        writeHttpData(data);
                    } finally {
                        data.release();
                    }
                }
            }
        } catch (HttpPostRequestDecoder.EndOfDataDecoderException e1) {
        }
    }
    private void writeHttpData(InterfaceHttpData data) {
        if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            String value;
            Map<String, List<String>> map = new HashMap();
            try {
                logger.info("HttpServer收到对方的请求数据为："+attribute.getName()+"="+attribute.getValue());
                value = attribute.getValue();
                String[] values = value.split(",");
                map.put(attribute.getName(),new ArrayList<String>((Arrays.asList(values))));
            } catch (IOException e1) {
                logger.error("读取POST参数失败！");
                e1.printStackTrace();
                return;
            }
            params.putAll(map);
        }
    }
    private void writeResponse(ChannelHandlerContext ctx,String uri) {
        // Decide whether to close the connection or not.
        boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.headers().get(CONNECTION))
                || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.headers().get(CONNECTION));

        // Build the response object.
        FullHttpResponse response = null;
        if("/processor/T60002".equals(uri)){
            response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(txnContext.getMsgtoa60002().getBytes()));
            logger.info("HttpServer本地响应数据为："+FbiBeanUtils.decode64(txnContext.getMsgtoa60002().toString()));
        }else{
            response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(txnContext.getMsgtoa().getBytes()));
            logger.info("HttpServer本地响应数据为："+FbiBeanUtils.decode64(txnContext.getMsgtoa().toString()));
        }

        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

        if (!close) {
            // There's no need to add 'Content-Length' header
            // if this is the last response.
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        }

        Set<Cookie> cookies;
        String value = request.headers().get(COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = CookieDecoder.decode(value);
        }
        if (!cookies.isEmpty()) {
            // Reset the cookies if necessary.
            for (Cookie cookie : cookies) {
                response.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookie));
            }
        }
        // Write the response.
        ChannelFuture future = ctx.channel().writeAndFlush(response);
        // Close the connection after the write operation is done if necessary.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
