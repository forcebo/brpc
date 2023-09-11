package com.lwb;


import com.lwb.discovery.Registry;
import com.lwb.discovery.impl.ZookeeperRegistry;
import lombok.extern.slf4j.Slf4j;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class BRpcBootStrap {

    private static final BRpcBootStrap bRpcBootStrap = new BRpcBootStrap();

    private String appName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private int port = 9088;
    //注册中心
    private Registry registry;
    //维护已经发布且暴露的服务列表 key -> interface的全限名称  value -> ServiceConfig
    private static final Map<String, ServiceConfig<?>> SERVERS_LIST = new ConcurrentHashMap<>(16);

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
        this.appName = appName;
        return this;
    }

    /**
     * 用来配置一个注册中心
     * @return this
     */
    public BRpcBootStrap registry(RegistryConfig registryConfig) {
        this.registry = registryConfig.getRegistry();
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocolConfig 协议的封装
     * @return this
     */
    public BRpcBootStrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
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
        // 抽象了注册中心的概念，使用注册中心的一个实现
        registry.register(service);

        //当服务调用方，通过接口、方法名、具体的方法参数列表发起调用，服务提供方要知道使用哪个实现
        //1. new 一个 2.spring beanFactory.getBean(Class) 3.自己维护映射关系
        SERVERS_LIST.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * 批量发布服务
     * @param services 封装需要发布的服务集合
     * @return this
     */
    public BRpcBootStrap publish(List<ServiceConfig<?>> services) {
        for (ServiceConfig<?> service : services) {
            this.publish(service);
        }
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        try {
            Thread.sleep(2000000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * -------------------------服务调用方相关API------------------------
     */

    public BRpcBootStrap reference(ReferenceConfig<?> reference) {
        // 在这个方法里我们是否可以拿到相关的配置项-注册中心
        // 配置reference， 将来调用get方法时，方便获取代理对象
        reference.setRegistry(registry);
        return this;
    }
}
