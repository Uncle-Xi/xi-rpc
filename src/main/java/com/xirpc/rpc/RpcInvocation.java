package com.xirpc.rpc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: RpcInvocation
 * ...
 * @author: Uncle.Xi 2020
 * @since: 1.0
 * @Environment: JDK1.8 + CentOS7.x + ?
 */
public class RpcInvocation implements Invocation, Serializable {

    private static final long serialVersionUID = 0L;

    private String methodName;

    private Class<?>[] parameterTypes;

    private Object[] arguments;

    private Class<?> type;

    public RpcInvocation(Class<?> type, String methodName, Class<?>[] parameterTypes, Object[] arguments) {
        this.type = type;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes;
        this.arguments = arguments == null ? new Object[0] : arguments;
    }

    public RpcInvocation(String methodName, Class<?>[] parameterTypes, Object[] arguments) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes;
        this.arguments = arguments == null ? new Object[0] : arguments;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public Class<?> getType() {
        return type;
    }
}
