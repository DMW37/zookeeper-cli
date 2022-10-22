package org.example.rewrite;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

/**
 * @auther 邓明维
 * @date 2022/8/3
 * @version:1.0
 * @description 分布式锁的实现
 * 实现步骤：
 * 1.新建连接
 * 2.
 */
public class DistributedLock implements Watcher, AsyncCallback.StatCallback, AsyncCallback.StringCallback, AsyncCallback.ChildrenCallback {

    private ZooKeeper zooKeeper;
    private String threadName;
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private String nodeName;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public DistributedLock(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void tryLock() {
        zooKeeper.create("/lock", this.threadName.getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, this, "create");
        //创建临时节点，只有一个人会抢到锁，其余的会进入到阻塞状态
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void unLock() {
        try {
            // 释放锁
            zooKeeper.delete(this.getNodeName(), -1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        //创建临时序列节点后的回调方法
        if (name != null) {
            // 创建后回调的名称 /xxxxxx001
            // 存储节点名称
            this.setNodeName(name);
            // 获取目录下子节点的所有名称
            zooKeeper.getChildren("/", false, this, "children");

        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children) {
        // 每创建一个临时序列节点信息，就立即获取当前父目录下有哪些临时序列节点信息，然后进行排序操作
        Collections.sort(children);
        String pathName = this.getNodeName().substring(1);
        int index = children.indexOf(pathName);
        if (index == 0) {
            // 如果它是第一个临时序列化的节点，则获得锁
            // 向/设置数据的目录就是简单的增加延时，让zk有足够的时间监听回调
            System.out.println("I am first ...");
            try {
                this.zooKeeper.setData("/", this.getThreadName().getBytes(StandardCharsets.UTF_8), -1);
                this.countDownLatch.countDown();
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            // 不是第一个，则监听前一个 既要监听，又要回调
            this.zooKeeper.exists("/" + children.get(index - 1), this, this, "exists");
        }
    }


    @Override
    public void process(WatchedEvent event) {
        // watch的事件回调
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                // 被删除了，通知后面的监听者，枪锁
                zooKeeper.getChildren("/", false, this, "children");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
        }
    }

    @Override
    public void processResult(int rc, String path, Object ctx, Stat stat) {

    }
}
