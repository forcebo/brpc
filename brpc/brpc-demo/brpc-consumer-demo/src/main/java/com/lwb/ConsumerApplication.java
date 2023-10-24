package com.lwb;


import com.lwb.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        // 想尽一切办法获取代理对象， 使用ReferenceConfig进行封装
        // reference一定用生成代理的模板方法，get();
        ReferenceConfig<HelloBRpc> reference = new ReferenceConfig<>();
        reference.setInterface(HelloBRpc.class);
        //代理做了些什么
        // 1.连接服务列表
        // 2.拉取服务列表
        // 3.选择一个服务并建立连接
        // 4.发送请求，携带一些信息（接口名， 参数列表，方法的名字），获取结果
        BRpcBootStrap.getInstance()
                .application("first-BRpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("hessian")
                .compress("gzip")
                .reference(reference);

        //获取一个代理对象
        HelloBRpc helloBRpc = reference.get();
        for (int i = 0; i < 10; i ++) {
            String sayHi = helloBRpc.sayHi("你好brpc");
            log.info("sayHi-->{}", sayHi);
        }
    }
}
