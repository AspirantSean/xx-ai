package com.dbapp.extension.ai.management.runtime.jvm;

import com.dbapp.extension.ai.management.AIModelManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RuntimeHook {

    @Resource
    private AIModelManager aiModelManager;

    @PostConstruct
    private void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.warn("application is shutdown, start kill ai extension process...");
            aiModelManager.destroyAll();
            log.warn("ai extension process is killed.");
        }));
    }

}
