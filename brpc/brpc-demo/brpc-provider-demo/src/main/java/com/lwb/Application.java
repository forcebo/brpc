package com.lwb;

import com.lwb.impl.HelloBRpcImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {
    public static void main(String[] args) {
        // 服务提供方，需要注册服务，启动服务
        // 1. 封装要发布的服务
        ServiceConfig<HelloBRpc> service = new ServiceConfig<>();
        service.setInterface(HelloBRpc.class);
        service.setRef(new HelloBRpcImpl());
        // 2. 定义注册中心

        // 3. 通过启动引导程序，启动服务提供方
        // （1） 配置 -- 应用的名称 -- 注册中心 -- 序列化协议 -- 压缩方式
        // （2）发布服务
        BRpcBootStrap.getInstance()
                .application("first-BRpc-provider")
                .registry(new RegistryConfig("Zookeeper://127.0.0.1:2181"))
                .protocol(new ProtocolConfig("jdk"))
                .publish(service)
                .start();

    }
}
