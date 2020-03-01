package com.xirpc.remoting.transport;

import com.xirpc.common.URL;


public interface Transporter {

    Server bind(URL url, ChannelHandler handler) throws Exception;

    Client connect(URL url, ChannelHandler handler) throws Exception;
}
