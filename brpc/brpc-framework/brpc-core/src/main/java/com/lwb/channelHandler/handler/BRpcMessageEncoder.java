package com.lwb.channelHandler.handler;

import com.lwb.enumeration.RequestType;
import com.lwb.transport.message.BRpcRequest;
import com.lwb.transport.message.MessageFormatConstant;
import com.lwb.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 自定义协议编码器
 * 4B magic(魔术值) --->brpc.getBytes()
 * 1B version(版本) ---> 1
 * 2B header length 首部的长度
 * 4B full length 报文总长度
 * 1B serialize
 * 1B compress
 * 1B requestType
 * 8B requestId
 * body
 *
 * 出站时，第一个经过的处理器
 */
@Slf4j
public class BRpcMessageEncoder extends MessageToByteEncoder<BRpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, BRpcRequest bRpcRequest, ByteBuf byteBuf) throws Exception {
        //header
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        // full length
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FILED_LENGTH);
        byteBuf.writeByte(bRpcRequest.getRequestType());
        byteBuf.writeByte(bRpcRequest.getCompressType());
        byteBuf.writeByte(bRpcRequest.getSerializeType());
        byteBuf.writeLong(bRpcRequest.getRequestId());
        //判断是否是心跳请求
        // body(requestPayload)
        byte[] body = getBodyBytes(bRpcRequest.getRequestPayload());
        if (body != null){
            byteBuf.writeBytes(body);
        }
        int bodyLength = body == null? 0 : body.length;
        // 先保存当前写指针的位置
        int writerIndex = byteBuf.writerIndex();
        // 将写指针移到之前位置
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH
                + MessageFormatConstant.HEADER_FILED_LENGTH);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH + bodyLength);
        //归位
        byteBuf.writerIndex(writerIndex);
    }

    private byte[] getBodyBytes(RequestPayload requestPayload) {
        // todo: 针对不同的消息类型需要做不同的处理，如心跳的请求
        if (requestPayload == null) {
            return null;
        }
        try {
            //序列化
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(baos);
            outputStream.writeObject(requestPayload);
            //压缩
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("序列化时出现异常");
            throw new RuntimeException(e);
        }
    }
}
