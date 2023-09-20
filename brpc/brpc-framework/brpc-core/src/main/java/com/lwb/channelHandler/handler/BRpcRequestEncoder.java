package com.lwb.channelHandler.handler;

import com.lwb.BRpcBootStrap;
import com.lwb.exceptions.SerializerException;
import com.lwb.serialize.Serializer;
import com.lwb.serialize.SerializerFactory;
import com.lwb.transport.message.BRpcRequest;
import com.lwb.transport.message.MessageFormatConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

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
public class BRpcRequestEncoder extends MessageToByteEncoder<BRpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, BRpcRequest bRpcRequest, ByteBuf byteBuf) throws Exception {
        //header
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        // full length
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FILED_LENGTH);
        byteBuf.writeByte(bRpcRequest.getRequestType());
        byteBuf.writeByte(bRpcRequest.getSerializeType());
        byteBuf.writeByte(bRpcRequest.getCompressType());
        byteBuf.writeLong(bRpcRequest.getRequestId());
        //判断是否是心跳请求
        // body(requestPayload)
        // 根据配置的序列化方式进行序列化, 解耦合()
        Serializer serializer = SerializerFactory.getSerializer(BRpcBootStrap.SERIALIZE_TYPE).getSerializer();
        if (serializer == null) {
            throw new SerializerException();
        }
        byte[] body = serializer.serialize(bRpcRequest.getRequestPayload());
        // 根据配置的压缩方式进行压缩
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
        if (log.isDebugEnabled()) {
            log.debug("请求【{}】已经完成报文的编码。",bRpcRequest.getRequestId());
        }
    }

}
