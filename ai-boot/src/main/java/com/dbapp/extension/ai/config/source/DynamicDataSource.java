package com.dbapp.extension.ai.config.source;


import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Map;

@Slf4j
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        //log.info("当前数据源：{}", DataSourceContextHolder.getDataBaseType());
        return DataSourceContextHolder.getDataBaseType();
    }

    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

}
