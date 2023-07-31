package com.dbapp.extension.sync.components;

import com.alibaba.fastjson.JSON;
import com.dbapp.extension.sync.model.ao.DatabaseConfig;
import com.dbapp.extension.sync.model.ao.DatasourceDefinition;
import com.dbapp.extension.sync.model.ao.SyncTableIDefinition;
import com.dbapp.extension.sync.util.SystemProperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@EnableAsync
@Configuration
public class DatabaseConfiguration {

    /**
     * 启动时进行配置解析
     */
    @Bean(name = "datasourceDefinition")
    public DatasourceDefinition datasourceDefinition() throws IOException {
        DatabaseConfig databaseConfig = JSON.parseObject(
                SystemProperUtil.getResourcesAsInputStream("sync" + SystemProperUtil.getFileSeparator() + "schema.json"),
                DatabaseConfig.class);
        if (databaseConfig == null) {
            return null;
        }
        try {
            return DatasourceDefinition.builder()
                    .databaseConfig(databaseConfig)
                    .syncTableIDefinitions(databaseConfig.getNodes()
                            .stream()
                            .peek(syncTableConfig -> syncTableConfig.setBelongTo(databaseConfig).setParent(null))
                            .map(syncTableConfig -> SyncTableIDefinition.builder()
                                    .syncTableConfig(syncTableConfig)
                                    .sql(syncTableConfig.queryDataSql())// 查询语句
                                    .mapping(syncTableConfig.mapping())// 对象映射
                                    .template(databaseConfig.template(syncTableConfig))// elasticsearch 模板
                                    .build())
                            .collect(Collectors.toList()))
                    .build();
        } catch (Exception e) {
            log.error("创建同步配置Definition对象失败", e);
            throw e;
        }
    }


}
