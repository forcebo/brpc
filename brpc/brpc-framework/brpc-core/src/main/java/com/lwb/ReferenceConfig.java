package com.lwb;

import com.lwb.discovery.NettyBootStrapInitializer;
import com.lwb.discovery.Registry;
import com.lwb.exceptions.NetWorkException;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
                // q:连接如果放在这的话，就意味着每次调用都会产生一个新的netty连接
                // 解决方案：缓存channel，尝试从缓存中获取channel，如果未获取，则创建新的连接并进行缓存,定义在BRpcBootStrap中
                Channel channel = BRpcBootStrap.CHANNEL_CACHE.get(address);
                CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
                if (channel == null) {
                    // await方法会阻塞，等待连接成功再返回
                    // sync和await都是阻塞当前线程，获取返回值（连接和发送数据的过程都是异步的）
                    // sync会前者会抛出异常，
                    NettyBootStrapInitializer.getBootstrap().connect(address).addListener(
                            (ChannelFutureListener) promise -> {
                                if (promise.isDone()) {
                                    // 异步的
                                    if(log.isDebugEnabled()) {
                                        log.debug("已经和[{}]成功建立了连接", address);
                                    }
                                    channelFuture.complete(promise.channel());
                                } else if (!promise.isSuccess()) {
                                    channelFuture.completeExceptionally(promise.cause());
                                }
                            }
                    );
                    //阻塞获取结果
                    channel = channelFuture.get(3, TimeUnit.SECONDS);
                    //缓存channel
                    BRpcBootStrap.CHANNEL_CACHE.put(address, channel);
                }
                if (channel == null) {
                    log.error("获取或建立与[{}]的通道时发生了异常。", address);
                    throw new NetWorkException("获取通道发生异常！");
                }
                /**
                 * ----------------同步策略---------------------
                 */
                /*
                ChannelFuture channelFuture = channel.writeAndFlush(new Object()).await();
                // get 阻塞获取结果， getNow 获取当前结果， 如果未完成， 返回null
                if (channelFuture.isDone()) {
                    Object object = channelFuture.getNow();
                } else if (!channelFuture.isSuccess()) {
                    // 失败则捕获异常
                    Throwable cause = channelFuture.cause();
                    throw new RuntimeException(cause);
                }
                */
                /**
                 * ---------------异步策略---------------------
                 */

                //todo: 需要将completableFuture 暴露出去
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                channel.writeAndFlush(new Object()).addListener((ChannelFutureListener) promise -> {
                    if (promise.isDone()) {
                        completableFuture.complete(promise.getNow());
                    } else if (promise.isSuccess()) {
                        completableFuture.completeExceptionally(promise.cause());
                    }
                 });
                return completableFuture.get(3, TimeUnit.SECONDS);
            }
        });
        return (T) helloProxy;
    }
}
