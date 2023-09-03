package com.rpc;

import com.rpc.netty.MyWatcher;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ZookeeperTest {
    ZooKeeper zooKeeper;

    @Before
    public void create() {
        String connectString = "127.0.0.1:2181";
        int timeout = 10000;
        try {
            zooKeeper = new ZooKeeper(connectString,timeout,null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testCreatePNode() {
        try {
            String result = zooKeeper.create("/lwb", "hello".getBytes(StandardCharsets.UTF_8)
                    , ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("result = " + result);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testDeletePNode() {
        try {
            zooKeeper.delete("/lwb", -1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Test
    public void testExistsPNode() {
        try {
            Stat stat = zooKeeper.exists("/lwb", null);

            int version = stat.getVersion(); // 当前节点数据版本
            System.out.println("version = " + version);
            int aversion = stat.getAversion(); //当前节点的acl数据版本
            System.out.println("aversion = " + aversion);
            int cversion = stat.getCversion(); // 当前子节点的版本
            System.out.println("cversion = " + cversion);
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Test
    public void testWatcher() {
        try {
            zooKeeper.exists("/lwb", new MyWatcher());

            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
