package com.lwb.discovery;

import com.lwb.channelHandler.ConsumerChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * NettyBootStrap单例
 * todo: 可能会存在问题
 */
@Slf4j
public class NettyBootStrapInitializer {

    private static final Bootstrap bootstrap = new Bootstrap();


    static {
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                // 选择初始化一个怎么样的Channel
                .channel(NioSocketChannel.class)
                .handler(new ConsumerChannelInitializer());
    }

    private NettyBootStrapInitializer() {
    }

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }
}
