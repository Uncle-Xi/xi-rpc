package com.xirpc.protocol;

import com.summerframework.boot.SummerApplication;
import com.summerframework.core.logger.LogFactory;
import com.xicp.util.StringUtils;
import com.xirpc.common.URL;
import com.xirpc.config.ServiceBean;
import com.xirpc.remoting.transport.NettyClient;
import com.xirpc.remoting.transport.NettyClientHandler;
import com.xirpc.rpc.Invocation;
import com.xirpc.rpc.Invoker;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @description: XiCPInvoker
 * ...
 * @author: Uncle.Xi 2020
 * @since: 1.0
 * @Environment: JDK1.8 + CentOS7.x + ?
 */
public class XiCPInvoker<T> implements Invoker {

    private static final LogFactory logger = new LogFactory(XiCPInvoker.class);
    private String className;
    private Class<?> type;
    private ServiceBean serviceBean;
    NettyClientHandler handler;

    public XiCPInvoker(String className, ServiceBean serviceBean) throws ClassNotFoundException {
        this.className = className;
        this.type = Class.forName(className);
        this.serviceBean = serviceBean;
        this.handler = new NettyClientHandler();
    }

    @Override
    public Class getInterface() {
        return type;
    }

    @Override
    public Object invoke(Invocation invocation) throws Throwable {
        //NettyClient client = ServiceBean.clientMap.get(className);
        //if (client == null) {
        //    System.out.println("XiCPInvoker invoke client == null [className] -> " + className);
        //    URL serviceUrl = getUrl();
        //    System.out.println("serviceUrl -> " + serviceUrl);
        //    client = (NettyClient) ServiceBean.transporter.connect(serviceUrl, handler);
        //    //client.doOpen(); //client.doConnect();
        //    ServiceBean.clientMap.put(className, client);
        //}
        //System.out.println("invoke send [StringUtils.getString(invocation)] -> " + StringUtils.getString(invocation));
        ////handler.send(invocation);
        ////return client.doConnect(invocation);
        ////return handler.getFuture();
        //return client.syncSend(invocation);

        return new NettyClient().send(getUrl(), invocation);
        //return serviceBean.transporter.connect(getUrl(), invocation);
    }

    @Override
    public URL getUrl() {
        Set<URL> urls = serviceBean.xiCPRegistry.getRemoteServiceList(className);
        if (urls == null || urls.isEmpty()) {
            URL url = new URL(null, null, 0, className);
            serviceBean.xiCPRegistry.subscribe(url);
        }
        urls = serviceBean.xiCPRegistry.getRemoteServiceList(className);
        return loadBalance(urls);
    }

    private URL loadBalance(Set<URL> urls) {
        logger.info("[loadBalance] [urls.size]=[" + (urls == null ? "0" : urls.size()) + "]");
        if (urls == null || urls.isEmpty()) {
            throw new RuntimeException("可用服务为空！");
        }
        int index = new Random().nextInt(urls.size());
        int i = 0;
        URL u = null;
        for (URL url : urls) {
            if (i++ == index) {
                u = url;
                break;
            }
        }
        return u == null ? null : u;
    }


    public static void main(String[] args) {
        Set<String> set = new HashSet<>();
        set.add("");
        int index = new Random().nextInt(set.size());
        System.out.println(index);
    }
}
