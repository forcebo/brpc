package com.lwb.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 它用来描述，请求调用方所请求的接口方法的描述
 * helloBRpc.sayHi("你好");
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestPayload implements Serializable {

    private String interfaceName;

    private String methodName;

    private Class<?>[] parametersType;

    private Object[] parametersValues;

    private Class<?> returnType;
}
