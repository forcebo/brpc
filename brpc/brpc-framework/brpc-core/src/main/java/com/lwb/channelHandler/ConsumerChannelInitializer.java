package com.lwb.channelHandler;

import com.lwb.channelHandler.handler.MySimpleChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //pipeline 流水线
        socketChannel.pipeline().addLast(new MySimpleChannelInboundHandler());
    }
}
