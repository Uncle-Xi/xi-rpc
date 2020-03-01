package com.xirpc.rpc;

public interface Invocation {

    String getMethodName();

    Class<?>[] getParameterTypes();

    Object[] getArguments();

    Class<?> getType();
}
