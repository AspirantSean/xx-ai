package com.dbapp.extension.sync.file.impl;

import com.dbapp.extension.sync.util.GlobalAttribute;
import com.dbapp.extension.sync.file.FileWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.regex.Pattern;

@Component
public class GlobalPropertiesWatcher implements FileWatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalPropertiesWatcher.class);
    private Pattern pattern = Pattern.compile("^global.*\\.properties$");

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
        }
        //删除事件
        if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
            LOGGER.error("The global.properties has deleted! Check it!");
            GlobalAttribute.init();
        }
    }
}
