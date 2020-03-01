package com.xirpc.remoting.transport;

import com.xirpc.common.URL;
import com.xirpc.common.UrlUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @description: NettyClient
 * ...
 * @author: Uncle.Xi 2020
 * @since: 1.0
 * @Environment: JDK1.8 + CentOS7.x + ?
 */
public class NettyClient implements Client {

    private Bootstrap bootstrap;
    private URL url;
    private InetSocketAddress bindAddress;
    private ChannelHandler channelHandler;
    ChannelFuture future;
    //EventLoopGroup group = new NioEventLoopGroup();
    //final ClientHandler clientHandler = new ClientHandler();

    public NettyClient(){

    }

    public NettyClient(final URL url, ChannelHandler channelHandler) throws Exception {
        this.url = url;
        this.bindAddress = UrlUtil.urlToInetSocketAddress(url);
        this.channelHandler = channelHandler;
    }

    @Override
    public void doOpen() throws Throwable {
//        try {
//            bootstrap = new Bootstrap();
//            bootstrap.group(group);
//            bootstrap.channel(NioSocketChannel.class);
//            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
//            bootstrap.option(ChannelOption.TCP_NODELAY, true);
//            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
//                @Override
//                public void initChannel(SocketChannel ch) throws Exception {
//                    ChannelPipeline pipeline = ch.pipeline();
//                    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
//                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
//                    pipeline.addLast("encoder", new ObjectEncoder());
//                    pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
//                    pipeline.addLast("handler", channelHandler);
//                }
//            });
//            future = bootstrap.connect(bindAddress.getHostName(), bindAddress.getPort()).sync();
//            future.channel().closeFuture().sync();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            group.shutdownGracefully();
//        }
    }

    @Override
    public void doConnect() throws Throwable {
        //System.out.printf("NettyClient doConnect -> \n", bindAddress.getHostName(), bindAddress.getPort());
        //ChannelFuture future = bootstrap.connect(bindAddress.getHostName(), bindAddress.getPort());
        //future.sync();
        //future.channel().closeFuture().sync();
        //future.channel().writeAndFlush(msg).sync();
    }

    protected void doClose() {

    }


    public Object syncSend(Object msg) throws IOException {
        ObjectOutputStream out = null;
        ByteArrayInputStream in = null;
        try {
            Socket socket = new Socket(bindAddress.getHostName(), bindAddress.getPort());
            out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(msg);
            out.flush();
            byte[] buff = new byte[1024];
            int len = 0;
            if ((len = socket.getInputStream().read(buff)) > 0) {
                in = new ByteArrayInputStream(buff, 0, len);
            }
            return new ObjectInputStream(in).readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
        return null;
    }

    public Object send(URL url, Object msg) {
        EventLoopGroup group = new NioEventLoopGroup();
        ClientHandler clientHandler = new ClientHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                            pipeline.addLast("encoder", new ObjectEncoder());
                            pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast("handler", clientHandler);
                        }
                    });

            ChannelFuture future = bootstrap.connect(url.getHost(), url.getPort()).sync();
            future.channel().writeAndFlush(msg).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
        return clientHandler.getResponse();
    }

    @io.netty.channel.ChannelHandler.Sharable
    class ClientHandler extends ChannelInboundHandlerAdapter {
        private Object response;
        public Object getResponse() {
            return response;
        }
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            response = msg;
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            // TODO 区分框架异常与业务异常！
            cause.printStackTrace();
            ctx.close();
        }
    }
}
