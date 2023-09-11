package com.lwb;

import com.lwb.discovery.Registry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;

    private Registry registry;

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    /**
     * 代理设计模式，生成一个api接口的代理对象
     * @return
     */
    public T get() {
        // 使用动态代理
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};

        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 我们调用sayHi方法，事实上会走进这个代码段中
                log.info("method-->{}", method.getName());
                log.info("args-->{}", args);

                //1.传入服务的名字，获取服务提供方地址
                // todo q:每次调用相关方法的时候都需要去注册中心拉取服务列表吗？ 本地缓存 + watcher
                //  我们如何合理的选择一个可用的服务，而不是只获取第一个  负载均衡
                InetSocketAddress address = registry.lookup(interfaceRef.getName());
                if (log.isDebugEnabled()) {
                    log.debug("服务调用方，返回了服务[{}]的可用主机[{}]", interfaceRef.getName(), address);
                }
                //2.使用netty连接服务器
                return null;
            }
        });
        return (T) helloProxy;
    }
}
