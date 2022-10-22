package org.example;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        System.out.println("Hello World!");
        // 没有连接池的概念
        // 连接字符串使用 逗号 分开
        // sessionTimeout ：断开后，保持3秒
        // Watcher : 在创建监听的时候，这个watcher 是 session 级别的，和path node没有关系
        // watch ： 只会在get 是可以注册
        // 当我们new Zookeeper时，是异步的，它先会给你返回一个对象
        // 当我们客户端连接的zk几点挂掉，知识点：1.new zk是session级别的watch，当session发生事件，会被回调 2.当客户端连接的zk出现问题，则会切换会话到其它机器 3.切换后其sessionId是不会改变的
        final CountDownLatch cd = new CountDownLatch(1);
        final ZooKeeper zooKeeper = new ZooKeeper("192.168.150.129,192.168.150.130,192.168.150.131", 3000, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println("new :" + watchedEvent.toString());
                switch (watchedEvent.getState()) {
                    case Unknown:
                        break;
                    case Disconnected:
                        break;
                    case NoSyncConnected:
                        break;
                    case SyncConnected:
                        System.out.println("connected");
                        cd.countDown();
                        break;
                    case AuthFailed:
                        break;
                    case ConnectedReadOnly:
                        break;
                    case SaslAuthenticated:
                        break;
                    case Expired:
                        break;
                }
                switch (watchedEvent.getType()) {
                    case None:
                        break;
                    case NodeCreated:
                        break;
                    case NodeDeleted:
                        break;
                    case NodeDataChanged:
                        break;
                    case NodeChildrenChanged:
                        break;
                }
            }
        });
        // 阻塞
        cd.await();
        ZooKeeper.States state = zooKeeper.getState();
        switch (state) {
            case CONNECTING:
                System.out.println("ing......");
                break;
            case ASSOCIATING:
                break;
            case CONNECTED:
                System.out.println("ed......");
                break;
            case CONNECTEDREADONLY:
                break;
            case CLOSED:
                break;
            case AUTH_FAILED:
                break;
            case NOT_CONNECTED:
                break;
        }

        // 创建
        final Stat stat = new Stat();
        String pathNode = zooKeeper.create(
                "/xxoo",
                "老数据".getBytes(StandardCharsets.UTF_8),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL);

        byte[] node = zooKeeper.getData("/xxoo", new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println("回调:" + watchedEvent.toString());
                try {
                    // 继续注册监听
                    // true 表示默认监听，default watch 被重新注册，new zk的那个
                    zooKeeper.getData("/xxoo", this, stat);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, stat);

        System.out.println(new String(node));

        // 修改数据

        Stat stat1 = zooKeeper.setData("/xxoo", "修改数据1".getBytes(StandardCharsets.UTF_8), 0);
        Stat stat2 = zooKeeper.setData("/xxoo", "修改数据2".getBytes(StandardCharsets.UTF_8), stat1.getVersion());

        // 获取数据，异步回调
        System.out.println("---start");
        zooKeeper.getData("/xxoo", false, new AsyncCallback.DataCallback() {
            @Override
            public void processResult(int rc, String path, Object ctx, byte[] bytes, Stat stat) {
                System.out.println("---back");
                System.out.println(new String(bytes));
                System.out.println(ctx);
            }
        }, "可以想传递的参数");
        System.out.println("---over");
        System.in.read();
    }
}
