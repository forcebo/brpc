package com.lwb.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡器接口
 */
public interface LoadBalancer {
    /**
     * 根据服务名，找到可用的服务
     * @param serviceName
     * @return
     */
    InetSocketAddress selectServiceAddress(String serviceName);



}
