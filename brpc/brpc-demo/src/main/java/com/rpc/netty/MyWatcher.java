package com.rpc.netty;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class MyWatcher implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        // 判断事件类型，连接类型的事件
        if (watchedEvent.getType() == Event.EventType.None) {
            if(watchedEvent.getState() == Event.KeeperState.AuthFailed) {
                System.out.println("zookeeper认证失败");
            }else if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                System.out.println("zookeeper连接成功");
            }else if (watchedEvent.getState() == Event.KeeperState.Disconnected) {
                System.out.println("zookeeper连接失败");
            }
        } else if (watchedEvent.getType() == Event.EventType.NodeCreated) {
            System.out.println(watchedEvent.getPath() + "被创建了");
        } else if (watchedEvent.getType() == Event.EventType.NodeDataChanged) {
            System.out.println(watchedEvent.getPath() + "的数据被修改了");
        } else if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
            System.out.println(watchedEvent.getPath() + "被删除了");
        }
    }
}
