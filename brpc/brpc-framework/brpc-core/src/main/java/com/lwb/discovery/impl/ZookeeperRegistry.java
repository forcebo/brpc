package com.lwb.discovery.impl;

import com.lwb.Constant;
import com.lwb.ServiceConfig;
import com.lwb.discovery.AbstractRegistry;
import com.lwb.exceptions.NetWorkException;
import com.lwb.utils.NetUtil;
import com.lwb.utils.zookeeper.ZookeeperNode;
import com.lwb.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {
    // 维护一个zk实例
    private final ZooKeeper zooKeeper;

    public ZookeeperRegistry() {
        this.zooKeeper = ZookeeperUtil.createZookeeper();
    }

    public ZookeeperRegistry(String connectString, int timeout) {
        this.zooKeeper = ZookeeperUtil.createZookeeper(connectString, timeout);
    }

    @Override
    public void register(ServiceConfig<?> service) {
        //服务名称的节点（持久节点）
        String parentNode = Constant.BASE_PROVIDERS_PATH + "/" + service.getInterface().getName();

        if(!ZookeeperUtil.exists(zooKeeper, parentNode, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode, null);
            ZookeeperUtil.createNode(zooKeeper, zookeeperNode, null, CreateMode.PERSISTENT);
        }
        // 创建本机的临时节点, ip:port
        // 服务提供方的端口一般自己设定， 还需要一个获取ip的方法
        // ip 我们通常是需要一个局域网ip， 而不是127.0.0.1，也不是ipv6
        // todo: 后续处理端口
        String node = parentNode + "/" + NetUtil.getIp() + ":" + 9088;
        if(!ZookeeperUtil.exists(zooKeeper, node, null)) {
            ZookeeperNode zookeeperNode = new ZookeeperNode(node, null);
            ZookeeperUtil.createNode(zooKeeper, zookeeperNode, null, CreateMode.EPHEMERAL);
        }
        if(log.isDebugEnabled()) {
            log.debug("服务{}， 已经被注册", service.getInterface().getName());
        }
    }

    @Override
    public InetSocketAddress lookup(String serviceName) {
        // 1.找到服务对应的节点
        String serviceNode = Constant.BASE_PROVIDERS_PATH + "/" + serviceName;

        // 2.从zk中获取他的子节点
        List<String> children = ZookeeperUtil.getChildren(zooKeeper, serviceNode, null);
        // 获取了所有可用的服务列表
        List<InetSocketAddress> inetSocketAddresses = children.stream().map(ipString -> {
            String[] ipAndPort = ipString.split(":");
            String ip = ipAndPort[0];
            int port = Integer.parseInt(ipAndPort[1]);
            return new InetSocketAddress(ip, port);
        }).toList();

        if(inetSocketAddresses == null || inetSocketAddresses.size() == 0) {
            throw new NetWorkException("未发现任何可用的服务主机！");
        }
        return inetSocketAddresses.get(0);
    }
}
