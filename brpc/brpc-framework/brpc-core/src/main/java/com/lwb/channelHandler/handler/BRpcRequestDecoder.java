package com.lwb.channelHandler.handler;

import com.lwb.enumeration.RequestType;
import com.lwb.transport.message.BRpcRequest;
import com.lwb.transport.message.MessageFormatConstant;
import com.lwb.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * 基于长度字段的帧解码器
 *
 *  * 自定义协议编码器
 *  * 4B magic(魔术值) --->brpc.getBytes()
 *  * 1B version(版本) ---> 1
 *  * 2B header length 首部的长度
 *  * 4B full length 报文总长度
 *  * 1B serialize
 *  * 1B compress
 *  * 1B requestType
 *  * 8B requestId
 *  * body
 */
@Slf4j
public class BRpcRequestDecoder extends LengthFieldBasedFrameDecoder {
    public BRpcRequestDecoder() {
        //找到当前报文的总长度
        super(
                //最大帧的长度，超过这个长度的直接丢掉
                MessageFormatConstant.MAX_FRAME_LENGTH,
                //长度的字段偏移量
                MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FILED_LENGTH,
                //长度的字段的长度
                MessageFormatConstant.FULL_FILED_LENGTH,
                // todo: 负载的适配长度
                -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH
                        + MessageFormatConstant.HEADER_FILED_LENGTH + MessageFormatConstant.FULL_FILED_LENGTH),
                0);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx,in);
        if (decode instanceof ByteBuf byteBuf) {
            return decodeFrame(byteBuf);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        // 解析报文
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        // 检测魔术值是否匹配
        for (int i = 0; i < magic.length; i ++) {
            if (magic[i] != MessageFormatConstant.MAGIC[i]) {
                throw new RuntimeException("The request obtained is not legitimate");
            }
        }
        //解析版本号
        byte version = byteBuf.readByte();
        if (version > MessageFormatConstant.VERSION) {
            throw new RuntimeException("获得的请求版本不被支持.");
        }
        //解析头部的长度
        short headLength = byteBuf.readShort();

        //解析总长度
        int fullLength = byteBuf.readInt();

        //请求类型
        byte requestType = byteBuf.readByte();
        //序列化类型
        byte serializeType = byteBuf.readByte();
        //压缩类型
        byte compressType = byteBuf.readByte();
        //请求id
        long requestId = byteBuf.readLong();

        int payloadLength = fullLength - headLength;
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);

        //封装
        BRpcRequest bRpcRequest = new BRpcRequest();
        bRpcRequest.setRequestType(requestType);
        bRpcRequest.setCompressType(compressType);
        bRpcRequest.setSerializeType(serializeType);
        bRpcRequest.setRequestId(requestId);

        //如果是心跳请求，直接返回
        if (requestType == RequestType.HEART_BEAT.getId()) {
            return bRpcRequest;
        }

        // todo: 解压缩

        // 反序列化
        try (ByteArrayInputStream bis = new ByteArrayInputStream(payload);
             ObjectInputStream ois = new ObjectInputStream(bis);
        ){
            RequestPayload requestPayload = (RequestPayload) ois.readObject();
            bRpcRequest.setRequestPayload(requestPayload);
        }catch (IOException | ClassNotFoundException e) {
            log.error("请求【{}】反序列化时发生了异常",requestId);
            throw new RuntimeException(e);
        }
        if (log.isDebugEnabled()) {
            log.debug("请求【{}】已经完成服务的解码。",bRpcRequest.getRequestId());
        }
        return bRpcRequest;
    }


}