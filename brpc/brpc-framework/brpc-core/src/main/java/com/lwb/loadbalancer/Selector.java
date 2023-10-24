package com.lwb.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

public interface Selector {

    /**
     * 根据服务列表执行一种算法获取一个服务节点
     * @param serviceList 服务列表
     * @return 具体的服务节点
     */
    InetSocketAddress getNext();

    /**
     * todo : 服务下线重新做负载均衡
     */
    void reBalance();
}
