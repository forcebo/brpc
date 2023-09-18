package com.lwb.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务调用方发起的请求内容
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BRpcRequest {

    private long requestId;
    private byte requestType;
    private byte compressType;
    private byte serializeType;
    //具体的消息体
    private RequestPayload requestPayload;
}
