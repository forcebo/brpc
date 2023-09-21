package com.lwb;


import com.lwb.channelHandler.handler.BRpcRequestDecoder;
import com.lwb.channelHandler.handler.BRpcResponseEncoder;
import com.lwb.channelHandler.handler.MethodCallHandler;
import com.lwb.discovery.Registry;
import com.lwb.discovery.RegistryConfig;
import com.lwb.utils.IdGenerator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;


import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class BRpcBootStrap {

    private static final BRpcBootStrap bRpcBootStrap = new BRpcBootStrap();

    private String appName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private int port = 9088;
    public static final IdGenerator ID_GENERATOR = new IdGenerator(1, 2);
    public static String SERIALIZE_TYPE = "jdk";
    public static String COMPRESS_TYPE = "gzip";
    //注册中心
    private Registry registry;

    //netty连接的缓存
    public final static Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

    //维护已经发布且暴露的服务列表 key -> interface的全限名称  value -> ServiceConfig
    public static final Map<String, ServiceConfig<?>> SERVERS_LIST = new ConcurrentHashMap<>(16);

    //定义全局的对外挂起的completableFuture接口
    public final static Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

    private BRpcBootStrap() {
        // 构造启动引导程序时要做初始化的事
    }
    public static BRpcBootStrap getInstance() {
        return bRpcBootStrap;
    }

    /**
     * -----------------------服务提供方相关API----------------------
     */

    /**
     * 用来定义当前应用的名字
     * @param appName 应用的名字
     * @return this
     */
    public BRpcBootStrap application(String appName){
        this.appName = appName;
        return this;
    }

    /**
     * 用来配置一个注册中心
     * @return this
     */
    public BRpcBootStrap registry(RegistryConfig registryConfig) {
        this.registry = registryConfig.getRegistry();
        return this;
    }

    /**
     * 配置当前暴露的服务使用的协议
     * @param protocolConfig 协议的封装
     * @return this
     */
    public BRpcBootStrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if (log.isDebugEnabled()) {
            log.debug("当前工程使用了哪个协议： " + protocolConfig.toString() + "协议进行序列化");
        }
        return this;
    }

    /**
     * 发布服务，将接口实现，注册到服务中心
     * @param service 封装需要发布的服务
     * @return this
     */
    public BRpcBootStrap publish(ServiceConfig<?> service) {
        // 抽象了注册中心的概念，使用注册中心的一个实现
        registry.register(service);

        //当服务调用方，通过接口、方法名、具体的方法参数列表发起调用，服务提供方要知道使用哪个实现
        //1. new 一个 2.spring beanFactory.getBean(Class) 3.自己维护映射关系
        SERVERS_LIST.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * 批量发布服务
     * @param services 封装需要发布的服务集合
     * @return this
     */
    public BRpcBootStrap publish(List<ServiceConfig<?>> services) {
        for (ServiceConfig<?> service : services) {
            this.publish(service);
        }
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        // 1. 创建EventLoop， 老板只负责请求，之后会将请求分发至worker
        EventLoopGroup boss = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup(10);
        try {
            //2.创建一个服务引导程序
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //3.配置服务器
            serverBootstrap =  serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //核心，需要添加出栈和入栈的handler
                            socketChannel.pipeline().addLast(new LoggingHandler())
                                    .addLast(new BRpcRequestDecoder())
                                    //根据请求进行方法调用
                                    .addLast(new MethodCallHandler())
                                    .addLast(new BRpcResponseEncoder());
                        }
                    });
            //4.绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            //5.优雅关闭
            channelFuture.channel().closeFuture().sync();
        }catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * -------------------------服务调用方相关API------------------------
     */

    public BRpcBootStrap reference(ReferenceConfig<?> reference) {
        // 在这个方法里我们是否可以拿到相关的配置项-注册中心
        // 配置reference， 将来调用get方法时，方便获取代理对象
        reference.setRegistry(registry);
        return this;
    }

    public BRpcBootStrap serialize(String serializeType) {
        SERIALIZE_TYPE = serializeType;
        if(log.isDebugEnabled()) {
            log.debug("我们配置的序列化方式为【{}】", serializeType);
        }
        return this;
    }

    public BRpcBootStrap compress(String compressType) {
        COMPRESS_TYPE = compressType;
        if (log.isDebugEnabled()) {
            log.debug("我们配置了使用压缩算法为【{}】", compressType);
        }
        return this;
    }
}
