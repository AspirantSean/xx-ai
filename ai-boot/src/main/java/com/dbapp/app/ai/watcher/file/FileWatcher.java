package com.dbapp.app.ai.watcher.file;

import java.nio.file.WatchEvent;

public interface FileWatcher {
    /**
     * 文件名匹配
     *
     * @param name
     * @return
     */
    boolean matchFile(String name);

    /**
     * 触发事件
     * @param kind
     */
    void run(WatchEvent.Kind<?> kind);
}
