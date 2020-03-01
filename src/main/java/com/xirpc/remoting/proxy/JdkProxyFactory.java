package com.xirpc.remoting.proxy;

import com.xirpc.remoting.transport.ProxyFactory;
import com.xirpc.rpc.Invoker;
import com.xirpc.rpc.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @description: JdkProxyFactory
 * ...
 * @author: Uncle.Xi 2020
 * @since: 1.0
 * @Environment: JDK1.8 + CentOS7.x + ?
 */
public class JdkProxyFactory implements ProxyFactory , InvocationHandler {

    private Invoker<?> invoker;

    public JdkProxyFactory(){

    }

    @Override
    public Object getProxy(Invoker invoker) throws Exception {
        this.invoker = invoker;
        Class<?> clazz = invoker.getInterface();
        System.out.println("getProxy clazz -> " + clazz);
        //return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), this);
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(invoker, args);
        }
        if ("toString".equals(methodName) && parameterTypes.length == 0) {
            return invoker.toString();
        }
        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
            return invoker.hashCode();
        }
        if ("equals".equals(methodName) && parameterTypes.length == 1) {
            return invoker.equals(args[0]);
        }
        return invoker.invoke(new RpcInvocation(invoker.getInterface(), methodName, method.getParameterTypes(), args));
    }
}
