package com.xirpc.remoting.transport;

import com.summerframework.beans.factory.support.AnnotationConfigUtils;
import com.summerframework.core.logger.LogFactory;
import com.xirpc.config.ServiceConfig;
import com.xirpc.registry.xicp.XiCPRegistry;
import com.xirpc.rpc.RpcInvocation;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @description: NettyHandler
 * ...
 * @author: Uncle.Xi 2020
 * @since: 1.0
 * @Environment: JDK1.8 + CentOS7.x + ?
 */
@io.netty.channel.ChannelHandler.Sharable
public class NettyServiceHandler extends ChannelHandler {

    private static final LogFactory logger = new LogFactory(NettyServiceHandler.class);
    ServiceConfig config;
    ChannelHandlerContext ctx;

    public NettyServiceHandler(ServiceConfig config){
        this.config = config;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelActive...");
        this.ctx = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcInvocation request = (RpcInvocation)msg;
        String beanName = ServiceConfig.aliasInf2ImplMap.get(request.getType().getName());
        Object bean = ServiceConfig.providerBeanMap.get(beanName);
        Method method;
        //System.out.println("bean is null ??? -> " + bean);
        //System.out.println("bean is null beanName -> " + beanName);
        //System.out.println("ServiceConfig.providerBeanMap.size() -> " + ServiceConfig.providerBeanMap.size());
        //System.out.println("request.getType().toString() ??? -> " + bean.getClass().getName());
        //Object original = AnnotationConfigUtils.getJdkDynamicProxyTargetObject(bean);
        if (request.getParameterTypes() == null || request.getParameterTypes().length == 0) {
            method = bean.getClass().getMethod(request.getMethodName());
        } else {
            method = bean.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
        }
        Object result = method.invoke(bean, request.getArguments());
        send(result);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // TODO 区分框架异常与业务异常！
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    void send(Object message) throws Exception {
        while (ctx == null){
            Thread.sleep(100);
            System.out.println("NettyServiceHandler ChannelHandlerContext is null...");
        }
        ctx.write(message);
        ctx.flush();
        ctx.close();
    }
}
