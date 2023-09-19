package com.lwb.channelHandler;

import com.lwb.channelHandler.handler.BRpcRequestEncoder;
import com.lwb.channelHandler.handler.BRpcResponseDecoder;
import com.lwb.channelHandler.handler.BRpcResponseEncoder;
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
                // netty自带日志
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                // 消息编码器
                .addLast(new BRpcRequestEncoder())
                //入栈的解码器
                .addLast(new BRpcResponseDecoder())
                // 处理结果
                .addLast(new MySimpleChannelInboundHandler());
    }
}
