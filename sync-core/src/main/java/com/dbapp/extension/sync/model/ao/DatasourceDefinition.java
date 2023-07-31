package com.dbapp.extension.sync.model.ao;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DatasourceDefinition {
    /**
     * 所属数据库
     */
    private DatabaseConfig databaseConfig;
    /**
     * 同步配置定义
     */
    private List<SyncTableIDefinition> syncTableIDefinitions;
    /**
     * database.schema.tabla
     */
    private String syncVersionRecordTable;
}
