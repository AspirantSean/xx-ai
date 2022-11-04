package com.dbapp.extension.ai;

import com.dbapp.extension.ai.ha.ZookeeperProps;
import com.dbapp.extension.ai.ha.ZookeeperPropsWithLinux;
import com.dbapp.extension.ai.util.ZkCuratorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.data.Stat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContextException;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @ClassName ExtApplication
 * @Description 启动类
 * @Version 1.0-SNAPSHOT
 **/
@EnableFeignClients(basePackages = {"com.dbapp.extension.*.rpc"})
@SpringBootApplication
@EnableScheduling
@EnableAsync
@Slf4j
public class ExtApplication {

    public static void main(String[] args) {
        try {
            //增加 zk 分布式锁
            zkLock("/lock_ha/lock_ai");
            SpringApplication application = new SpringApplication(ExtApplication.class);
            application.run(args);
        } catch (ApplicationContextException e) {
            log.error("启动失败", e);
            System.exit(0);
        }
    }

    /**
     * 获取应用锁，保证在高可用场景下，只有一个应用正常启动
     *
     * @param lock_node 锁节点，一般放入：/lock_ha
     */
    private static void zkLock(String lock_node) {
        try {
            ZkCuratorUtil zkCuratorUtil = new ZkCuratorUtil(getZookeeperUrl(), "");
            zkCuratorUtil.start();
            Stat stat = zkCuratorUtil.checkExists(lock_node);
            if (stat == null) {
                zkCuratorUtil.create(lock_node);
            }
            long start = System.currentTimeMillis();
            log.info("Try to obtain the app lock...");
            zkCuratorUtil.getLock(lock_node).acquire();
            log.info("Obtaining the app lock successfully，it takes：" + (System.currentTimeMillis() - start) / 1000 + "s");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static String getZookeeperUrl() throws Exception {
        ZookeeperProps zookeeperProps = new ZookeeperPropsWithLinux();
        // 默认值
        return zookeeperProps.getUrl();
    }


}
