package com.xirpc.remoting.proxy;

import com.xirpc.remoting.transport.NettyTransporter;
import com.xirpc.rpc.Invoker;
import com.xirpc.rpc.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @description: InvokerInvocationHandler
 * ...
 * @author: Uncle.Xi 2020
 * @since: 1.0
 * @Environment: JDK1.8 + CentOS7.x + ?
 */
public class InvokerInvocationHandler {

//    private final Invoker<?> invoker;
//
//    public InvokerInvocationHandler(Invoker<?> handler) {
//        this.invoker = handler;
//    }
//
//    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        String methodName = method.getName();
//        Class<?>[] parameterTypes = method.getParameterTypes();
//        if (method.getDeclaringClass() == Object.class) {
//            return method.invoke(invoker, args);
//        }
//        if ("toString".equals(methodName) && parameterTypes.length == 0) {
//            return invoker.toString();
//        }
//        if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
//            return invoker.hashCode();
//        }
//        if ("equals".equals(methodName) && parameterTypes.length == 1) {
//            return invoker.equals(args[0]);
//        }
//        return invoker.invoke(new RpcInvocation(invoker.getInterface(), methodName, method.getParameterTypes(), args));
//    }
}
