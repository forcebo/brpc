package com.lwb;

import com.lwb.utils.zookeeper.ZookeeperNode;
import com.lwb.utils.zookeeper.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.util.List;

@Slf4j
public class Application {
    public static void main(String[] args) {
        ZooKeeper zooKeeper = ZookeeperUtil.createZookeeper();
        // 定义节点和数据
        String basePath = "/brpc-metadata";
        String providersPath = basePath + "/providers";
        String consumersPath = basePath + "/consumers";
        ZookeeperNode baseNode = new ZookeeperNode(basePath, null);
        ZookeeperNode providersNode = new ZookeeperNode(providersPath, null);
        ZookeeperNode consumersNode = new ZookeeperNode(consumersPath, null);
        List.of(baseNode, providersNode, consumersNode).forEach(node -> {
            ZookeeperUtil.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
        });
        ZookeeperUtil.close(zooKeeper);
    }
}
