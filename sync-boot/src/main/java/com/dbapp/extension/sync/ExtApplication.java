package com.dbapp.extension.sync;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContextException;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @ClassName ExtApplication
 * @Description 启动类
 * @Version 1.0-SNAPSHOT
 **/
@Slf4j
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class ExtApplication {
    public static void main(String[] args) {
        try {
            SpringApplication application = new SpringApplication(ExtApplication.class);
            application.run(args);
        } catch (ApplicationContextException e) {
            log.error("启动失败", e);
            System.exit(0);
        }
    }
}
