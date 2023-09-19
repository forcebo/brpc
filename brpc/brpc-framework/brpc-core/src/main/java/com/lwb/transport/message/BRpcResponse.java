package com.lwb.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务提供方回复的响应内容
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BRpcResponse {

    private long requestId;

    private byte compressType;
    private byte serializeType;
    // 自定义响应码 1 成功 2 异常
    private byte code;
    //具体的响应体
    private Object body;
}
