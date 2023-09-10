package com.lwb;


import com.lwb.utils.NetUtil;
import com.lwb.utils.zookeeper.ZookeeperNode;
import com.lwb.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

@Slf4j
public class BRpcBootStrap {

    private static final BRpcBootStrap bRpcBootStrap = new BRpcBootStrap();

    private String appName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private int port = 9088;
    //维护一个zookeeper实例
    private ZooKeeper zooKeeper;

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
        //这里维护一个zookeeper实例，但是，如果这样写就会将zookeeper和当前工程耦合（以后扩展）
        zooKeeper = ZookeeperUtil.createZookeeper();
        this.registryConfig = registryConfig;
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
        //服务名称的节点（持久节点）
        System.out.println(service.getInterface().getName());
        String parentNode =Constant.BASE_PROVIDERS_PATH + "/" + service.getInterface().getName();

        if(!ZookeeperUtil.exists(zooKeeper, parentNode, null)) {
            System.out.println(1);
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            ZookeeperUtil.createNode(zooKeeper, zookeeperNode, null, CreateMode.PERSISTENT);
        }

        // 创建本机的临时节点, ip:port
        // 服务提供方的端口一般自己设定， 还需要一个获取ip的方法
        // ip 我们通常是需要一个局域网ip， 而不是127.0.0.1，也不是ipv6
        String node = parentNode + "/" + NetUtil.getIp() + ":" + port;
        if(!ZookeeperUtil.exists(zooKeeper, node, null)) {
            System.out.println(123);
            ZookeeperNode zookeeperNode = new ZookeeperNode(node, null);
            ZookeeperUtil.createNode(zooKeeper, zookeeperNode, null, CreateMode.EPHEMERAL);
        }
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
        try {
            Thread.sleep(20000);
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
        return this;
    }
}
