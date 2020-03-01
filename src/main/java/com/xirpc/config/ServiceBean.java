package com.xirpc.config;

import com.summerframework.beans.Aware;
import com.summerframework.beans.BeansException;
import com.summerframework.beans.InitializingBean;
import com.summerframework.beans.factory.support.AnnotationConfigUtils;
import com.summerframework.beans.factory.support.BeanDefinitionReaderUtils;
import com.summerframework.boot.web.server.HttpServlet;
import com.summerframework.context.ApplicationContext;
import com.summerframework.context.ApplicationContextAware;
import com.summerframework.context.ApplicationEventPublisher;
import com.summerframework.context.ApplicationListener;
import com.summerframework.context.annotation.Bean;
import com.summerframework.context.event.SourceFilteringListener;
import com.summerframework.context.support.AbstractApplicationContext;
import com.summerframework.core.annotation.Order;
import com.summerframework.core.env.ConfigurableEnvironment;
import com.summerframework.core.logger.LogFactory;
import com.summerframework.core.util.ClassUtils;
import com.summerframework.web.context.event.ContextRefreshedEvent;
import com.xicp.client.ConnectStringParser;
import com.xicp.util.StringUtils;
import com.xirpc.config.annotation.Provider;
import com.xirpc.config.annotation.Reference;
import com.xirpc.protocol.XiCPInvoker;
import com.xirpc.remoting.proxy.JdkProxyFactory;
import com.xirpc.remoting.transport.ProxyFactory;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @description: ServiceBean
 * ...
 * @author: Uncle.Xi 2020
 * @since: 1.0
 * @Environment: JDK1.8 + CentOS7.x + ?
 */
@Order
public class ServiceBean extends ServiceConfig implements InitializingBean, ApplicationContextAware,
        ApplicationListener<ContextRefreshedEvent>, Aware, ApplicationEventPublisher {

    private static final LogFactory logger = new LogFactory(ServiceBean.class);

    private static final String BASE_PACKAGE = "xi.rpc.scan.base-package";
    private static final String REGISTRY_ADDRESS = "xi.rpc.registry.address";
    private static final String PROTOCOL_NAME = "xi.rpc.protocol.name";
    private static final String PROTOCOL_PORT = "xi.rpc.protocol.port";
    private static final String DEFAULT_PROTOCOL = "xirpc";
    private static final int DEFAULT_PORT = 20880;
    private static boolean isJar = false;

    private AbstractApplicationContext applicationContext;
    private ConfigurableEnvironment environment;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (AbstractApplicationContext) applicationContext;
        this.environment = this.applicationContext.getEnvironment();
        this.publishEvent(this);
        logger.info("ServiceBean setApplicationContext ...");
    }

    @Override
    public void publishEvent(Object o) {
        this.applicationContext.
                addApplicationListener(new SourceFilteringListener(applicationContext, (ApplicationListener<?>) o));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //logger.error("任务描述：1、拿到对外服务列表上报 xicp 监听 20880 2、从 xicp 定时抓取消费服务 3、生成服务消费代理对象提前放入 IOC 中");
        connectString = environment.getProperty(REGISTRY_ADDRESS);
        connectString = connectString == null? "" : connectString.substring(connectString.indexOf("//")).replaceAll("/", "");
        protocol = environment.getProperty(PROTOCOL_NAME, DEFAULT_PROTOCOL);
        port = Integer.parseInt(environment.getProperty(PROTOCOL_PORT, String.valueOf(DEFAULT_PORT)));
        proxyFactory = new JdkProxyFactory();
        findHosts();
        initXiRpc();
        AddAnnotation.addAnnotation(Reference.class);
    }

    private void findHosts(){
        try (Socket socket = new Socket()) {
//            ConnectStringParser connectStringParser = new ConnectStringParser(connectString);
//            SocketAddress addr = connectStringParser.getServerAddresses().get(0);
//            System.out.println("addr -> " + addr);
//            System.out.println("connectString -> " + connectString);
//            socket.connect(addr, 1000);
//            host = socket.getLocalAddress().getHostAddress();
            InetAddress inetAddress = Inet4Address.getLocalHost();
            System.out.println(inetAddress.getHostAddress());
            host = inetAddress.getHostAddress();
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private void initXiRpc() throws Exception {
        String basePackage = environment.getProperty(BASE_PACKAGE);
        if (StringUtils.isEmpty(basePackage)) { return; }
        logger.info("initXiRpc -> " + basePackage);
        Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader()
                .getResources(basePackage.replace(".", StringUtils.FOLDER_SEPARATOR));
        logger.info(basePackage.replace(".", StringUtils.FOLDER_SEPARATOR));
        while (urlEnumeration.hasMoreElements()) {
            URL url = urlEnumeration.nextElement();
            String protocol = url.getProtocol();
            if ("jar".equalsIgnoreCase(protocol)) {
                doScanJar(basePackage, url);
            } else {
                doScanLocalPackage(basePackage);
                break;
            }
        }
        if (isEmpty()) { return; }
        startServer();
        initClient();
    }

    protected void initClient() throws Exception {
        if (consumerClassList.isEmpty()) {
            return;
        }
        //logger.info("TODO 生成代理对象，注入到 IOC 容器！");
        for (String consumer : consumerClassList) {
            System.out.println("consumer -> " + consumer);
            consumerBeanMap.put(consumer, proxyFactory.getProxy(new XiCPInvoker(consumer, this)));
        }
        logger.info("consumerBeanMap.size() ->  " + consumerBeanMap.size());
        for (Map.Entry<String, Object> entry : consumerBeanMap.entrySet()) {
            //System.out.println("entry.getKey() -> " + entry.getKey());
            //System.out.println("entry.getValue() -> " + entry.getValue());
            applicationContext.registerBeanAndDefinition(entry.getKey(), entry.getValue());
        }
    }

    protected boolean isEmpty(){
        return providerClassList.isEmpty() && consumerClassList.isEmpty();
    }

    protected void doScanJar(String basePackage, URL url) throws Exception {
        JarURLConnection connection = (JarURLConnection) url.openConnection();
        if (connection != null) {
            JarFile jarFile = connection.getJarFile();
            if (jarFile != null) {
                Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                while (jarEntryEnumeration.hasMoreElements()) {
                    JarEntry entry = jarEntryEnumeration.nextElement();
                    String jarEntryName = entry.getName();
                    if (!jarEntryName.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) { continue; }
                    boolean isServiceClass = jarEntryName.contains(ClassUtils.CLASS_FILE_SUFFIX)
                            && jarEntryName.replaceAll(StringUtils.FOLDER_SEPARATOR, StringUtils.CURRENT_PATH)
                            .startsWith(basePackage);
                    if (!isServiceClass)                                      { continue; }
                    String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(StringUtils.CURRENT_PATH))
                            .replaceAll(StringUtils.FOLDER_SEPARATOR, StringUtils.CURRENT_PATH);
                    addBeanMap(className);
                }
            }
        }
    }

    static class AddAnnotation extends AnnotationConfigUtils{
        public static void addAnnotation(Class<?> clazz){
            addAutowiredAnnotations(clazz);
        }
    }

    protected void doScanLocalPackage(String daoPackage) throws Exception {
        URL url = ClassUtils.getDefaultClassLoader().getResource(
                daoPackage.replaceAll("\\.", StringUtils.FOLDER_SEPARATOR));
        if (url == null) { return; }
        File classPath = new File(url.getFile());
        if (classPath == null) { return; }
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanLocalPackage(daoPackage + StringUtils.CURRENT_PATH + file.getName());
            } else {
                if (!file.getName().endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
                    continue;
                }
                String className = getClassName(daoPackage, file.getName());
                //System.out.println("doScanLocalPackage -> " + className);
                addBeanMap(className);
            }
        }
    }

    private String getClassName(String basePackage, String fileName){
        return basePackage + StringUtils.CURRENT_PATH + fileName
                .replace(ClassUtils.CLASS_FILE_SUFFIX, StringUtils.NONE_SPACE);
    }

    protected void addBeanMap(String className) throws Exception {
        Class<?> beanClass = Class.forName(className);
        if (beanClass.isAnnotationPresent(Provider.class)) {
            String lastName = replaceName(beanClass);
            //logger.info("addBeanMap Provider last className -> " + lastName);
            providerClassList.add(lastName);
            return;
        } else {
            Field[] fields = beanClass.getDeclaredFields();
            Set<String> fieldList = new HashSet<>();
            for(Field field : fields){
                for (Annotation annotation : field.getAnnotations()){
                    if (annotation.annotationType().getName().equals(Reference.class.getTypeName())){
                        String fieldName = field.getType().getName();
                        fieldList.add(fieldName);
                        consumerClassList.add(fieldName);
                    }
                }
            }
            if (fieldList.isEmpty()) { return; }
            logger.info("addBeanMap Reference className -> " + className);
            consumerBean2ClassMap.put(className, fieldList);
        }
    }

    private static String replaceName(Class<?> beanClass) throws ClassNotFoundException {
        Class<?>[] interfaces = beanClass.getInterfaces();
        for (Class<?> ifc : interfaces) {
            aliasImpl2InfMap.put(ifc.getName(), ifc.getName());
            aliasImpl2InfMap.put(beanClass.getName(), ifc.getName());
            aliasInf2ImplMap.put(ifc.getName(), beanClass.getName());
            break;
        }
        aliasInf2ImplMap.put(beanClass.getName(), beanClass.getName());
        return beanClass.getName();
    }

    public static void main(String[] args) throws Exception {
        //System.out.println(Reference.class.getTypeName());
        Class<?> beanClass = Class.forName("com.experience.provider.HiXiRpcProvider");
        //Class<?> beanClass = Class.forName("com.experience.consumer.HiXiRpcConsumer");
        //Field[] fields = beanClass.getDeclaredFields();
        //for(Field field : fields){
        //    System.out.println(field.getName());
        //    System.out.println(field.getClass().getName());
        //    System.out.println(field.getType().getName());
        //    for (Annotation annotation : field.getAnnotations()){
        //        System.out.println(annotation.annotationType().getName());
        //    }
        //}

        System.out.println(replaceName(beanClass));
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        //logger.info("TODO 拿到 consumer 的代理对象，注入到目标类中");
        if (isEmpty()) {
            logger.info("【没有服务暴露和远程服务消费】");
            return;
        }
        //System.out.println("providerClassList.size() -> " + providerClassList.size());
        for (String provider : providerClassList) {
            Object bean = applicationContext.getBean(provider);
            //System.out.println("onApplicationEvent getBean() -> " + provider);
            //System.out.println("onApplicationEvent getBean() -> " + bean);
            providerBeanMap.put(provider, bean);
        }
    }
}
