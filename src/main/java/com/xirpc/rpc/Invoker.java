package com.xirpc.rpc;

import com.xirpc.common.Node;

public interface Invoker <T> extends Node {

    Class<T> getInterface();

    Object invoke(Invocation invocation) throws Throwable;
}