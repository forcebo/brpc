package com.lwb.discovery.impl;

import com.lwb.ServiceConfig;
import com.lwb.discovery.AbstractRegistry;

import java.net.InetSocketAddress;

public class NacosRegistry extends AbstractRegistry {

    public NacosRegistry() {
    }

    public NacosRegistry(String host, int timeout) {

    }

    @Override
    public void register(ServiceConfig<?> service) {

    }

    @Override
    public InetSocketAddress lookup(String name) {
        return null;
    }
}
