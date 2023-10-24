package com.lwb.discovery;

import com.lwb.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 注册中心
 */
public interface Registry {
    /**
     * 注册服务
     * @param service 服务的配置内容
     */
    void register(ServiceConfig<?> service);

    /**
     * 从注册中心拉取一个可用服务
     * @param serviceName 服务的名称
     * @return 服务的地址
     */
    List<InetSocketAddress> lookup(String serviceName);
}
