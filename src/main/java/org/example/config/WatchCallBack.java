package org.example.config;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.concurrent.CountDownLatch;

/**
 * @auther 邓明维
 * @date 2022/8/3
 * @version:1.0
 */
public class WatchCallBack implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback {
    private ZooKeeper zk;
    private MyConf myConf;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public WatchCallBack(ZooKeeper zk) {
        this.zk = zk;
    }

    public void setMyConf(MyConf myConf) {
        this.myConf = myConf;
    }

    public void aWait() {
        zk.exists("/appconfig", this, this, "ctx");
        // 程序阻塞
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processResult(int responseCode, String path, Object ctx, byte[] bytes, Stat stat) {
        if (bytes != null) {
            this.myConf.setConf(new String(bytes));
            // 当获取到数据了，放行
            countDownLatch.countDown();
        }
    }

    @Override
    public void processResult(int responseCode, String path, Object ctx, Stat stat) {
        //可以获取数据
        if (stat != null) {
            zk.getData("/appconfig", this, this, "ctx");
        }
    }

    /**
     * 如果被修改了，如何办
     *
     * @param watchedEvent
     */
    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                break;
            case NodeCreated:
                zk.getData("/appconfig", this, this, "ctx");
                break;
            case NodeDeleted:
                // 看你是否容忍容错性
                //1.清空配置
                //2.继续阻塞，等待数据
                this.myConf.setConf(null);
                this.countDownLatch = new CountDownLatch(1);
                break;
            case NodeDataChanged:
                // 被修改了数据，那么我们需要重新获取数据
                zk.getData("/appconfig", this, this, "ctx");
                break;
            case NodeChildrenChanged:
                break;
        }
    }
}
