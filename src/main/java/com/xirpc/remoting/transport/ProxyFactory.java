package com.xirpc.remoting.transport;

import com.xirpc.rpc.Invoker;

public interface ProxyFactory {

    <T> T getProxy(Invoker<T> invoker) throws Exception;
}
