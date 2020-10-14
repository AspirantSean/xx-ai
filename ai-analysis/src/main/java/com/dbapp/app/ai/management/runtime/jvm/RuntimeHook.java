package com.dbapp.app.ai.management.runtime.jvm;

import com.dbapp.app.ai.management.AIModelManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Component
public class RuntimeHook {

    @Resource
    private AIModelManager aiModelManager;

    @PostConstruct
    private void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.warn("application is shutdown, start kill ai app process...");
            aiModelManager.destroyAll();
            log.warn("ai app process is killed.");
        }));
    }

}