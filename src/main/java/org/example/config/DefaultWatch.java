package org.example.config;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.concurrent.CountDownLatch;

/**
 * @auther 邓明维
 * @date 2022/8/3
 * @version:1.0
 * @description watcher
 */
public class DefaultWatch implements Watcher {
    private CountDownLatch cdl;

    public DefaultWatch(CountDownLatch cdl) {
        this.cdl = cdl;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getState()) {
            case SyncConnected:
                cdl.countDown();
        }
    }
}
