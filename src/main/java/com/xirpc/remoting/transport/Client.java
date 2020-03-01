package com.xirpc.remoting.transport;

import com.xirpc.rpc.RpcInvocation;

public interface Client {

    void doOpen() throws Throwable;

    void doConnect() throws Throwable;
}
