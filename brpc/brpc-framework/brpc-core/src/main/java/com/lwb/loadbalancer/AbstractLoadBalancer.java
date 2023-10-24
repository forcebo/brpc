package com.lwb.loadbalancer;

import com.lwb.BRpcBootStrap;
import com.lwb.discovery.Registry;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模板方法
 */
public abstract class AbstractLoadBalancer implements LoadBalancer{
    // 一个服务会匹配一个selector
    private Map<String, Selector> cache = new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectServiceAddress(String serviceName) {
        Selector selector = cache.get(serviceName);
        if (selector == null){
            // 维护服务列表作为缓存
            List<InetSocketAddress> serviceList = BRpcBootStrap.getInstance().getRegistry().lookup(serviceName);
            selector = getSelector(serviceList);
            // 将selector放入缓存中
            cache.put(serviceName, selector);
        }

        // 获取可用节点
        return selector.getNext();

    }

    /**
     * 由子类进行扩展
     * @param serviceList 服务列表
     * @return 负载均衡算法选择器
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);
}
