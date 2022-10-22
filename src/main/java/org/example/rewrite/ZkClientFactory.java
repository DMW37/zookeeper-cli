package org.example.rewrite;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @auther 邓明维
 * @date 2022/8/3
 * @version:1.0
 * @description zk客户端工厂
 */
public class ZkClientFactory {
    private static String clientStr = "192.168.150.129,192.168.150.130,192.168.150.131/lock";
    private CountDownLatch countDownLatch = new CountDownLatch(1);


    public ZooKeeper build() {
        ZooKeeper zooKeeper = null;
        try {
            zooKeeper = new ZooKeeper(clientStr, 1000, new DefaultWatcher(this.countDownLatch));
            // 阻塞
            countDownLatch.await();
            return zooKeeper;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zooKeeper;
    }

}
