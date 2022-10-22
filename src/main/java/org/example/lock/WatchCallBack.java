package org.example.lock;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @auther 邓明维
 * @date 2022/8/3
 * @version:1.0
 */
public class WatchCallBack implements Watcher, AsyncCallback.StringCallback, AsyncCallback.ChildrenCallback, AsyncCallback.StatCallback {
    private ZooKeeper zooKeeper;
    private String threadName;
    private CountDownLatch cdl = new CountDownLatch(1);
    // 节点的路径名称
    private String pathName;


    public WatchCallBack(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void tryLock() {
        try {
            //创建临时序列节点
            zooKeeper.create("/lock", this.getThreadName().getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL, this, "lock");
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void unLock() {
        try {
            // - 1 忽略版本判定
            zooKeeper.delete(this.getPathName(), -1);
            System.out.println(this.getThreadName() + " :over");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     * 节点删除事件回调
     *
     * @param event
     */
    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                break;
            case NodeCreated:
                break;
            case NodeDeleted:
                // 关注删除的事件
                zooKeeper.getChildren("/", false, this, "lock");
                break;
            case NodeDataChanged:
                break;
            case NodeChildrenChanged:
                break;
        }
    }

    /**
     * 创建目录节点，然后获取目录的名称
     * @param rc
     * @param path
     * @param ctx
     * @param name
     */
    @Override
    public void processResult(int rc, String path, Object ctx, String name) {
        System.out.println(this.getThreadName() + " create node : " + name);
        if (name != null) {
            this.setPathName(name);
            // 不监听根目录，成本太大
            zooKeeper.getChildren("/", false, this, "lock");
        }
    }


    /**
     * 获取自己在目录中的位置，第一个获得锁
     *
     * @param rc
     * @param path
     * @param ctx
     * @param children
     */
    @Override
    public void processResult(int rc, String path, Object ctx, List<String> children) {
        // 一定可以观察到自己前面的节点
        // 排序，此时字符串中，是没有  / 开头的
        Collections.sort(children);
        String pathName = this.getPathName().substring(1);
        int index = children.indexOf(pathName);
        if (index == 0) {
            // 如果它是第一个临时序列化的节点，则获得锁
            // 向/设置数据的目录就是简单的增加延时，让zk有足够的时间监听回调
            System.out.println("I am first ...");
            try {
                this.zooKeeper.setData("/", this.getThreadName().getBytes(StandardCharsets.UTF_8), -1);
                this.cdl.countDown();
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
    public void processResult(int rc, String path, Object ctx, Stat stat) {

    }
}
