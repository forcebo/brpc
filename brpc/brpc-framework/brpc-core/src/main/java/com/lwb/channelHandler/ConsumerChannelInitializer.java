package com.lwb.channelHandler;

import com.lwb.channelHandler.handler.BRpcMessageEncoder;
import com.lwb.channelHandler.handler.MySimpleChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //pipeline 流水线
        socketChannel.pipeline()
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast(new BRpcMessageEncoder())
                .addLast(new MySimpleChannelInboundHandler());
    }
}
