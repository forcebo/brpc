package com.lwb.discovery;

import com.lwb.Constant;
import com.lwb.discovery.impl.NacosRegistry;
import com.lwb.discovery.impl.ZookeeperRegistry;
import com.lwb.exceptions.DiscoveryException;



public class RegistryConfig {

    //定义连接的url zookeeper://127.0.0.1:2181
    private final String connectString;

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }

    /**
     * 使用简单工厂
     * @return 注册中心
     */
    public Registry getRegistry() {
        String registryType = getRegistryType(connectString, true).toLowerCase().trim();
        if ("zookeeper".equals(registryType)) {
            String host = getRegistryType(connectString, false);
            return new ZookeeperRegistry(host, Constant.TIME_OUT);
        } else if ("nacos".equals(registryType)) {
            String host = getRegistryType(connectString, false);
            return new NacosRegistry(host, Constant.TIME_OUT);
        }
        throw new DiscoveryException("未获取注册中心！");
    }

    private String getRegistryType(String connectString, boolean ifType) {
        String[] typeAndHost = connectString.split("://");
        if (typeAndHost.length != 2) {
            throw new RuntimeException("给定的注册中心连接url不合法！");
        }
        if (ifType) {
            return typeAndHost[0];
        } else {
            return typeAndHost[1];
        }
    }
}
