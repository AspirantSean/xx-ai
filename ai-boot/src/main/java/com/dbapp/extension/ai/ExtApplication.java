package com.dbapp.extension.ai;

import cn.hutool.extra.spring.SpringUtil;
import com.dbapp.extension.ai.ha.ZookeeperProps;
import com.dbapp.extension.ai.ha.ZookeeperPropsWithLinux;
import com.dbapp.extension.ai.util.ZkCuratorUtil;
import com.dbapp.extension.base.service.IExtBaseInfoSubmitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
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
@EnableFeignClients(basePackages = {"com.dbapp.**.rpc"})
@SpringBootApplication
@EnableScheduling
@EnableAsync
@Slf4j
public class ExtApplication {

    public static void main(String[] args) {
        try {
            //增加 zk 分布式锁
            zkLock();
            SpringApplication application = new SpringApplication(ExtApplication.class);
            application.run(args);
            syncInitStatus();
            log.info("ai service has started!");
        } catch (ApplicationContextException e) {
            log.error("ai service failed to start!", e);
            System.exit(0);
        }
    }

    /**
     * 获取应用锁，保证在高可用场景下，只有一个应用正常启动</br>
     * 锁节点，放入：/lock_ha/lock_ai
     */
    private static void zkLock() {
        try {
            String lockNode = "/lock_ha/lock_ai";
            ZkCuratorUtil zkCuratorUtil = new ZkCuratorUtil(getZookeeperUrl(), "");
            CuratorFramework client = zkCuratorUtil.start();;
            long start = System.currentTimeMillis();
            log.info("Try to obtain the app lock...");
            InterProcessMutex lock = zkCuratorUtil.getLock(client, lockNode);
            lock.acquire();
            log.info("Obtaining the app lock successfully，it takes：" + (System.currentTimeMillis() - start) / 1000 + "s");
        } catch (Exception e) {
            log.error("Obtaining the app lock failed.", e);
            System.exit(0);
        }
    }

    private static String getZookeeperUrl() {
        ZookeeperProps zookeeperProps = new ZookeeperPropsWithLinux();
        // 默认值
        return zookeeperProps.getUrl();
    }

    private static void syncInitStatus() {
        IExtBaseInfoSubmitService extBaseInfoSubmitService = SpringUtil.getBean(IExtBaseInfoSubmitService.class);
        extBaseInfoSubmitService.submitInitStatus();
        log.info("ai has upload init state to auth!");
    }
}
