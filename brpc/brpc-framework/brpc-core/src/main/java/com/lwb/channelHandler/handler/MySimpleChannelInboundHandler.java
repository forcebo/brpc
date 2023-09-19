package com.lwb.channelHandler.handler;

import com.lwb.BRpcBootStrap;
import com.lwb.transport.message.BRpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<BRpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BRpcResponse bRpcResponse) throws Exception {
        // 这里拿到服务端执行的结果
        Object returnValue = bRpcResponse.getBody();
        // 从全体的挂起列表中得到与之匹配的待处理的 completableFuture
        CompletableFuture<Object> completableFuture = BRpcBootStrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(returnValue);
        if (log.isDebugEnabled()) {
            log.debug("已寻找到编号为【{}】的completableFuture,处理响应结果。",bRpcResponse.getRequestId());
        }
    }
}
