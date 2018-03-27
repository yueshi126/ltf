package org.fbi.ltf.server.httpserver;

/**
 * Created by zhanrui on 2014/11/6.
 */
public interface Processor {
    public void service(TxnContext ctx);
}
