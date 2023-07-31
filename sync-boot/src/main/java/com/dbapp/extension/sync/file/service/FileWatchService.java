package com.dbapp.extension.sync.file.service;

import com.dbapp.extension.sync.file.FileWatcher;
import com.dbapp.extension.sync.util.SystemProperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Slf4j
@Component
public class FileWatchService implements DisposableBean, Runnable {
    @Resource
    private List<FileWatcher> watchers;
    private Thread thread;

    public FileWatchService() {
        this.thread = new Thread(this);
        this.thread.start();
    }

    public void run() {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            final Path path = Paths.get(SystemProperUtil.getGlobalPropertiesPathResource().getFile().getParentFile().getAbsolutePath());
            //给path路径加上文件观察服务
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            // start an infinite loop
            while (true) {
                final WatchKey key = watchService.take();

                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    // get the filename for the event
                    final WatchEvent.Kind<?> kind = watchEvent.kind();
                    final Path filePath = (Path) watchEvent.context();
                    final String fileName = filePath.toString();
                    // print it out
                    log.info("{} -> {}", kind, fileName);

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    for (FileWatcher watcher : watchers) {
                        if (watcher.matchFile(fileName)) {
                            watcher.run(kind);
                        }
                    }

                }
                // reset the keyf
                boolean valid = key.reset();
                // exit loop if the key is not valid (if the directory was
                // deleted,for
                if (!valid) {
                    break;
                }
            }
        } catch (IOException | InterruptedException ex) {
            log.error(ex.getMessage(), ex);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void destroy() {
        log.info("Destroy FileWatchService.");
    }

}
