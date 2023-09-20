package com.lwb.channelHandler.handler;

import com.lwb.BRpcBootStrap;
import com.lwb.ServiceConfig;
import com.lwb.enumeration.RespCode;
import com.lwb.transport.message.BRpcRequest;
import com.lwb.transport.message.BRpcResponse;
import com.lwb.transport.message.RequestPayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<BRpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BRpcRequest bRpcRequest) throws Exception {
        //获取负载内容
        RequestPayload requestPayload = bRpcRequest.getRequestPayload();
        //根据负载内容进行方法调用
        Object result = callTargetMethod(requestPayload);
        if (log.isDebugEnabled()) {
            log.debug("请求【{}】已经在服务端完成方法调用。",bRpcRequest.getRequestId());
        }
        //封装响应
        BRpcResponse bRpcResponse = new BRpcResponse();
        bRpcResponse.setCode(RespCode.SUCCESS.getCode());
        bRpcResponse.setRequestId(bRpcRequest.getRequestId());
        bRpcResponse.setCompressType(bRpcRequest.getCompressType());
        bRpcResponse.setSerializeType(bRpcRequest.getSerializeType());
        bRpcResponse.setBody(result);
        //写出响应
        channelHandlerContext.channel().writeAndFlush(bRpcResponse);
    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parameterValue = requestPayload.getParametersValues();

        //寻找匹配的类完成调用
        ServiceConfig<?> serviceConfig = BRpcBootStrap.SERVERS_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();

        //通过反射调用 1. 获取反射对象 2. 执行invoke方法
        Object returnValue;
        try {
            Class<?> aClass = refImpl.getClass();
            Method method = aClass.getMethod(methodName, parametersType);
            returnValue = method.invoke(refImpl, parameterValue);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            log.error("调用请求【{}】的方法【{}】时发生了异常",interfaceName, methodName, e);
            throw new RuntimeException(e);
        }
        return returnValue;
    }
}
