package org.example.config;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.example.util.ZKUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @auther 邓明维
 * @date 2022/8/3
 * @version:1.0
 * @description 对分布式配置进行测试
 */
public class ConfigTest {

    private ZooKeeper zk;

    @Before
    public void before() {
        zk = ZKUtils.getZk();
    }

    @After
    public void after() throws InterruptedException {
        zk.close();
    }

    @Test
    public void getConfig() {
        WatchCallBack watchCallBack = new WatchCallBack(zk);
        MyConf myConf = new MyConf();
        watchCallBack.setMyConf(myConf);
        watchCallBack.aWait();

        while (true) {
            if (myConf.getConf()== null) {
                System.out.println("数据丢失......等待数据......");
                watchCallBack.aWait();
            } else {
                System.out.println(myConf.getConf());
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
