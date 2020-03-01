package com.xirpc.config;

import com.summerframework.core.logger.LogFactory;
import com.xirpc.common.URL;
import com.xirpc.common.UrlUtil;
import com.xirpc.registry.xicp.XiCPRegistry;
import com.xirpc.remoting.transport.NettyClient;
import com.xirpc.remoting.transport.NettyServiceHandler;
import com.xirpc.remoting.transport.NettyTransporter;
import com.xirpc.remoting.transport.ProxyFactory;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @description: service config
 * ...
 * @author: Uncle.Xi 2020
 * @since: 1.0
 * @Environment: JDK1.8 + CentOS7.x + ?
 */
public class ServiceConfig {

    private static final LogFactory logger = new LogFactory(ServiceConfig.class);
    public static Set<String> providerClassList = new HashSet<>();
    public static Set<String> consumerClassList = new HashSet<>();
    public static Map<String, Set<String>> consumerBean2ClassMap = new HashMap<>();
    public static Map<String, Object> providerBeanMap = new HashMap<>();
    public static Map<String, Object> consumerBeanMap = new HashMap<>();
    public static Map<String, String> aliasInf2ImplMap = new HashMap<>();
    public static Map<String, String> aliasImpl2InfMap = new HashMap<>();
    public static Map<String, NettyClient> clientMap = new HashMap<>();
    public static String protocol;
    public static NettyTransporter transporter = new NettyTransporter();
    public static String host;
    public static int port;
    String connectString;
    public ProxyFactory proxyFactory;
    public XiCPRegistry xiCPRegistry;

    protected void startServer() {
        try {
            if (providerClassList.isEmpty() && consumerClassList.isEmpty()) {
                logger.info("没有服务消费和服务提供！");
                return;
            }
            xiCPRegistry = new XiCPRegistry(connectString, consumerClassList);
            if (providerClassList.isEmpty()) {
                logger.info("没有服务提供！");
                return;
            }
            URL url = null;
            logger.info("上报服务！ provider.size() -> " + providerClassList.size());
            for (String provider : providerClassList) {
                System.out.println("aliasMap.get(provider) -> " + aliasImpl2InfMap.get(provider));
                url = UrlUtil.strToUrl(protocol, host, port, aliasImpl2InfMap.get(provider));
                System.out.println("startServer url -> " + url);
                xiCPRegistry.register(url);
            }
            transporter.bind(url, new NettyServiceHandler(this)).doOpen();
        } catch (Throwable e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws UnknownHostException, Exception {
//        InetAddress inetAddress = Inet4Address.getLocalHost();
//        System.out.println(inetAddress.getHostAddress());
        String connectString = "xicp://192.168.0.104:2181";
        System.out.println(connectString.substring(connectString.indexOf("//")).replaceAll("/", ""));
    }
}
