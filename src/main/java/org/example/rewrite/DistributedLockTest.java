package org.example.rewrite;

/**
 * @auther 邓明维
 * @date 2022/8/3
 * @version:1.0
 * @description
 */
public class DistributedLockTest {
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DistributedLock lock = new DistributedLock(new ZkClientFactory().build());
                    lock.setThreadName(Thread.currentThread().getName());
                    //枪锁
                    lock.tryLock();
                    //做事
                    System.out.println(lock.getThreadName()+" 干事中...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //释放锁
                    lock.unLock();
                }
            }).start();
        }
    }
}
