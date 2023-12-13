package com.dbapp.extension.ai.file.impl;

import com.dbapp.extension.ai.management.AIModelManager;
import com.dbapp.extension.ai.utils.GlobalAttribute;
import com.dbapp.extension.ai.file.FileWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.regex.Pattern;

@Component
public class GlobalPropertiesWatcher implements FileWatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalPropertiesWatcher.class);
    private Pattern pattern = Pattern.compile("^global.*\\.properties$");

    @Resource
    private AIModelManager aiModelManager;

    @Override
    public boolean matchFile(String name) {
        return pattern.matcher(name).matches();
    }

    @Override
    public void run(WatchEvent.Kind<?> kind) {
        //修改事件 || 创建事件
        if (kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_CREATE) {
            LOGGER.info("The global.properties has modified! Reload it now!");
            GlobalAttribute.init();
            // 通知ai任务
            aiModelManager.initializeJob();
        }
        //删除事件
        if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            LOGGER.error("The global.properties has deleted! Check it!");
            GlobalAttribute.init();
        }
    }
}
