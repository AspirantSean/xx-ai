package com.dbapp.extension.sync.model.ao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class SyncTableIDefinition {

    /**
     * 对应表配置
     */
    private SyncTableConfig syncTableConfig;
    /**
     * 查询sql
     */
    private String sql;
    /**
     * 对象映射关系
     */
    private Mapping mapping;
    /**
     * elasticsearch字段映射模板
     */
    private Map<String, Object> template;
    /**
     * 版本表名
     */
    private String versionTableName;

}
