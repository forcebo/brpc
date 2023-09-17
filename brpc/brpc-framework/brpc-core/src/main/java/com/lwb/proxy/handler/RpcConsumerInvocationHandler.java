package com.lwb.proxy.handler;

import com.lwb.BRpcBootStrap;
import com.lwb.discovery.NettyBootStrapInitializer;
import com.lwb.discovery.Registry;
import com.lwb.exceptions.DiscoveryException;
import com.lwb.exceptions.NetWorkException;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 该类封装了客户端通信的基础逻辑
 * 1.发现可用服务
 * 2.建立连接
 * 3.发送请求
 * 4.得到结果
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {

    private final Registry registry;

    private final Class<?> interfaceRef;

    public <T> RpcConsumerInvocationHandler(Registry registry, Class<T> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }

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
        //2.获取一个可用的通道
        Channel channel = getAvailableChannel(address);

        if(log.isDebugEnabled()) {
            log.debug("获取了和【{}】建立的连接通道,准备发送数据", address);
        }
        /*
         *--------------------封装报文-----------------------
         */

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
        //4.写出报文
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        BRpcBootStrap.PENDING_REQUEST.put(1L, completableFuture);
        channel.writeAndFlush(Unpooled.copiedBuffer("hello".getBytes())).addListener((ChannelFutureListener) promise -> {
            if (!promise.isSuccess()) {
                completableFuture.completeExceptionally(promise.cause());
            }
        });
        // 如果没有地方处理这个completableFuture, 这里会阻塞， 等待complete方法的执行
        // q: 在pipeLine中获取最终执行结果
        // 5.获取响应的结果
        return completableFuture.get(10, TimeUnit.SECONDS);
    }

    private Channel getAvailableChannel(InetSocketAddress address) {
        // q:连接如果放在这的话，就意味着每次调用都会产生一个新的netty连接
        // 解决方案：缓存channel，尝试从缓存中获取channel，如果未获取，则创建新的连接并进行缓存,定义在BRpcBootStrap中
        //1.先从缓存中拿
        Channel channel = BRpcBootStrap.CHANNEL_CACHE.get(address);
        CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
        //2.拿不到再创建
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
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取通道时发生异常.", e);
                throw new DiscoveryException(e);
            }
            //缓存channel
            BRpcBootStrap.CHANNEL_CACHE.put(address, channel);
        }
        if (channel == null) {
            log.error("获取或建立与[{}]的通道时发生了异常。", address);
            throw new NetWorkException("获取通道发生异常！");
        }
        return channel;
    }
}
