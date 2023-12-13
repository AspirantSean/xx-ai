package com.dbapp.extension.ai.es.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

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

    @Data
    public static  class EsConfig {
        private String version;
    }
}
