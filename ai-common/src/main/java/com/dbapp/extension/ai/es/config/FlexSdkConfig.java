package com.dbapp.extension.ai.es.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableConfigurationProperties
@Configuration
@ConfigurationProperties(
        prefix = "ailpha.flexsdk"
)
@Data
public class FlexSdkConfig {

    private String url;

    private String app;

    private String baseVersion = "2.0";

    private EsConfig esConfig;

    private long period = 100L;
    private long maxPeriod = TimeUnit.SECONDS.toMillis(1L);

    private int maxAttempts = 3;

    @Data
    public static  class EsConfig {
        private String version;
    }
}
