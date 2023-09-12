package com.lwb.discovery;

import com.lwb.BRpcBootStrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * NettyBootStrap单例
 * todo: 可能会存在问题
 */
public class NettyBootStrapInitializer {

    private static Bootstrap bootstrap = new Bootstrap();


    static {
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                // 选择初始化一个怎么样的Channel
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(null);
                    }
                });
    }

    private NettyBootStrapInitializer() {
    }

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }
}
