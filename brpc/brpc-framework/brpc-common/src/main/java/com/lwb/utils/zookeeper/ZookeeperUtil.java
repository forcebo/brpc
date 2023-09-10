package com.lwb.utils.zookeeper;

import com.lwb.Constant;
import com.lwb.exceptions.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZookeeperUtil {

    /**
     * 使用默认配置创建zookeeper实例
     * @return zookeeper实例
     */
    public static ZooKeeper createZookeeper() {
        String connectString = Constant.Default_ZK_CONNECT;
        int timeout = Constant.TIME_OUT;

        return createZookeeper(connectString, timeout);
    }

    public static ZooKeeper createZookeeper(String connectString, int timeout) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            final ZooKeeper zooKeeper = new ZooKeeper(connectString, timeout, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("客户端连接成功");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            log.error("创建zookeeper实例时发生异常：", e);
            throw new ZookeeperException();
        }
    }

    /**
     * 创建一个节点
     * @param zooKeeper zooKeeper
     * @param node 节点
     * @param watcher watcher实例
     * @param createMode 节点的类型
     * @return true: 成功创建 false: 已经存在 异常：抛出
     */
    public static boolean createNode(ZooKeeper zooKeeper, ZookeeperNode node, Watcher watcher, CreateMode createMode) {
        try {
            if (zooKeeper.exists(node.getNodePath(), null) == null) {
                String result = zooKeeper.create(node.getNodePath(), null,
                        ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("节点[{}], 成功创建", result);
                return true;
            } else {
                if (log.isDebugEnabled()) {
                    log.info("节点[{}]已经存在， 无需创建", node.getNodePath());
                }
                return false;
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("创建基础目录时出现问题", e);
            throw new ZookeeperException();
        }
    }

    /**
     * 关闭zookeeper的方法
     * @param zooKeeper
     */
    public static void close(ZooKeeper zooKeeper) {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper时发生问题", e);
            throw new ZookeeperException();
        }
    }
}
