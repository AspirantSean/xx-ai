package com.dbapp.extension.sync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @ClassName ExtApplication
 * @Description 启动类
 * @Version 1.0-SNAPSHOT
 **/
@Slf4j
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication(scanBasePackages = {"com.dbapp.*"})
public class SyncApplication {
    public static void main(String[] args) {
        try {
            SpringApplication application = new SpringApplication(SyncApplication.class);
            application.run(args);
        } catch (ApplicationContextException e) {
            log.error("启动失败", e);
            System.exit(0);
        }
    }
}
