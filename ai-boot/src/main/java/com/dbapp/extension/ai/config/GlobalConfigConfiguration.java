package com.dbapp.extension.ai.config;

import com.dbapp.boot.listener.CustomEnvironmentListener;
import com.dbapp.nacos.config.GlobalConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author steven.zhu
 * @version 1.0.0
 * @date 2023/12/6
 */
@Configuration(proxyBeanMethods = false)
public class GlobalConfigConfiguration {

    @Bean
    public GlobalConfig extGlobalConfig() {
        return CustomEnvironmentListener.globalConfig;
    }
}
