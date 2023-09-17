package com.lwb.channelHandler.handler;

import com.lwb.BRpcBootStrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf msg) throws Exception {
        // 这里拿到服务端执行的结果
        String result = msg.toString(Charset.defaultCharset());
        // 从全体的挂起列表中得到与之匹配的待处理的 completableFuture
        CompletableFuture<Object> completableFuture = BRpcBootStrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
    }
}
