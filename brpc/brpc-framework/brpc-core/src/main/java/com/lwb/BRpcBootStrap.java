package com.lwb;


import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class BRpcBootStrap {

    private static BRpcBootStrap bRpcBootStrap = new BRpcBootStrap();

    private BRpcBootStrap() {
        // 构造启动引导程序时要做初始化的事
    }
    public static BRpcBootStrap getInstance() {
        return bRpcBootStrap;
    }

    /**
     * -----------------------服务提供方相关API----------------------
     */

    /**
     * 用来定义当前应用的名字
     * @param appName 应用的名字
     * @return this
     */
    public BRpcBootStrap application(String appName){
        return this;
    }

    /**
     * 用来配置一个注册中心
     * @return this
     */
    public BRpcBootStrap registry(RegistryConfig registryConfig) {
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocolConfig 协议的封装
     * @return this
     */
    public BRpcBootStrap protocol(ProtocolConfig protocolConfig) {
        if (log.isDebugEnabled()) {
            log.debug("当前工程使用了哪个协议： " + protocolConfig.toString() + "协议进行序列化");
        }
        return this;
    }

    /**
     * 发布服务，将接口实现，注册到服务中心
     * @param service 封装需要发布的服务
     * @return this
     */
    public BRpcBootStrap publish(ServiceConfig<?> service) {
        if(log.isDebugEnabled()) {
            log.debug("服务{}， 已经被注册", service.getInterface().getName());
        }
        return this;
    }

    /**
     * 批量发布服务
     * @param services 封装需要发布的服务集合
     * @return this
     */
    public BRpcBootStrap publish(List<?> services) {
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {

    }

    /**
     * -------------------------服务调用方相关API------------------------
     */

    public BRpcBootStrap reference(ReferenceConfig<?> reference) {
        // 在这个方法里我们是否可以拿到相关的配置项-注册中心
        // 配置reference， 将来调用get方法时，方便获取代理对象
        return this;
    }
}
