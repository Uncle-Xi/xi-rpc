package com.xirpc.remoting.transport;

import com.xirpc.rpc.RpcInvocation;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;

/**
 * @description: NettyHandler
 * ...
 * @author: Uncle.Xi 2020
 * @since: 1.0
 * @Environment: JDK1.8 + CentOS7.x + ?
 */
public abstract class ChannelHandler extends ChannelInboundHandlerAdapter{

    abstract void send(Object message) throws Exception;
}
