package org.example.util;

import org.apache.zookeeper.ZooKeeper;
import org.example.config.DefaultWatch;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @auther 邓明维
 * @date 2022/8/3
 * @version:1.0
 */
public class ZKUtils {
    private static ZooKeeper zk;
    // 创建一个属于自己的一个目录，在自己目录下进行操作
    private static String address = "192.168.150.129,192.168.150.130,192.168.150.131/mylock";
    // 线程阻塞，让其创建完成后再返回zk
    private static CountDownLatch countDownLatch = new CountDownLatch(1);
    // 传递引用，创建完成后
    private static DefaultWatch watch = new DefaultWatch(countDownLatch);

    public static ZooKeeper getZk() {
        try {
            zk = new ZooKeeper(address, 1000, watch);
            countDownLatch.await();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zk;
    }
}
