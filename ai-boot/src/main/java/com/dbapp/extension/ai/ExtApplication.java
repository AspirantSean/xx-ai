package com.dbapp.extension.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
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
public class ExtApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ExtApplication.class);
        application.run(args);
    }
}
