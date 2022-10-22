package org.example.lock;


import org.apache.zookeeper.ZooKeeper;
import org.example.util.ZKUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * @auther 邓明维
 * @date 2022/8/3
 * @version:1.0
 */
public class LockTest {
    private ZooKeeper zk;

    @Before
    public void before() {
        zk = ZKUtils.getZk();
    }

    @After
    public void after() {
        try {
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLock() throws IOException {

        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //创建十个对象,枪锁
                    WatchCallBack watchCallBack = new WatchCallBack(zk);
                    watchCallBack.setThreadName(Thread.currentThread().getName());
                    // 枪锁
                    watchCallBack.tryLock();
                    //干事
                    System.out.println(watchCallBack.getThreadName() + "枪锁完毕，干事中......");
                    // 不延迟执行时间产生的问题，当第一个直接执行完毕后，很可能后者还在注册监听，当第一个线程结束后，后面再注册上后，已经没有了回调
                    //try {
                    //    Thread.sleep(1000);
                    //} catch (InterruptedException e) {
                    //    e.printStackTrace();
                    //}
                    //释放锁
                    watchCallBack.unLock();
                }
            }).start();
        }
        System.in.read();
    }
}
