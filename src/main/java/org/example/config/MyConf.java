package org.example.config;

/**
 * @auther 邓明维
 * @date 2022/8/3
 * @version:1.0
 * @description 跨线程时，供不同线程访问
 */
public class MyConf {
    private String conf;

    public String getConf() {
        return conf;
    }

    public void setConf(String conf) {
        this.conf = conf;
    }
}
