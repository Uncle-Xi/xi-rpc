package com.xirpc.remoting.transport;

import com.xirpc.rpc.RpcInvocation;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @description: NettyClientHandler
 * ...
 * @author: Uncle.Xi 2020
 * @since: 1.0
 * @Environment: JDK1.8 + CentOS7.x + ?
 */
public class NettyClientHandler extends ChannelHandler {

    ChannelHandlerContext ctx;
    LinkedBlockingQueue<Object> linkedBlockingQueue = new LinkedBlockingQueue();

    public Object getFuture() throws InterruptedException {
        return linkedBlockingQueue.take();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到服务端响应...");
        linkedBlockingQueue.put(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // TODO 区分框架异常与业务异常！
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void send(Object message) throws Exception {
        while (ctx == null){
            Thread.sleep(100);
            System.out.println("NettyClientHandler ChannelHandlerContext is null...");
        }
        ctx.write(message);
        ctx.flush();
        ctx.close();
    }
}
