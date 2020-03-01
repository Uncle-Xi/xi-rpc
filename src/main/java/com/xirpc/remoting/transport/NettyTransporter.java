package com.xirpc.remoting.transport;

import com.xirpc.common.URL;

/**
 * @description: NettyTransporter
 * ...
 * @author: Uncle.Xi 2020
 * @since: 1.0
 * @Environment: JDK1.8 + CentOS7.x + ?
 */
public class NettyTransporter implements Transporter {

    @Override
    public Server bind(URL url, ChannelHandler channelHandler) throws Exception {
        return new NettyServer(url, channelHandler);
    }

    @Override
    public Client connect(URL url, ChannelHandler channelHandler) throws Exception {
        return new NettyClient(url, channelHandler);
    }
}
