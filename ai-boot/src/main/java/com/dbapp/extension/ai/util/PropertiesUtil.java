package com.dbapp.extension.ai.util;

import com.dbapp.boot.listener.CustomEnvironmentListener;
import com.dbapp.nacos.config.CoreConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertiesUtil {
    /**
     * 从 auth 的 core.properties 的配置文件中获取配置值
     * @param key 配置项
     * @return 配置值
     */
    public static String fromCoreProperties(String key) {
        CoreConfig coreConfig = CustomEnvironmentListener.coreConfig;
        if (coreConfig == null) {
            return "";
        }
        return coreConfig.getString(key, "");
    }
}
