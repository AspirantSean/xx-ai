package com.dbapp.extension.sync.mapper;

import com.dbapp.extension.sync.model.dto.UpdateVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper
public interface SynchronizerMapper {

    int createVersionTable(@Param("database") String database,
                           @Param("schema") String schema,
                           @Param("syncPrimaryVersionTableName") String syncPrimaryVersionTableName);

    boolean existTrigger(@Param("triggerName") String triggerName);

    int createFirstTrigger(@Param("database") String database,
                           @Param("schema") String schema,
                           @Param("functionName") String functionName,
                           @Param("syncPrimaryVersionTableName") String syncPrimaryVersionTableName,
                           @Param("id") String id,
                           @Param("notifyTableName") String notifyTableName);

    int createDirectEffectTrigger(@Param("database") String database,
                                  @Param("schema") String schema,
                                  @Param("functionName") String functionName,
                                  @Param("syncPrimaryVersionTableName") String syncPrimaryVersionTableName,
                                  @Param("id") String id,
                                  @Param("notifyTableName") String notifyTableName);

    int createIndirectEffectTrigger(@Param("database") String database,
                                    @Param("schema") String schema,
                                    @Param("functionName") String functionName,
                                    @Param("syncPrimaryVersionTableName") String syncPrimaryVersionTableName,
                                    @Param("id") String id,
                                    @Param("notifyTableName") String notifyTableName,
                                    @Param("fetchSql") String fetchSql);

    boolean existTable(@Param("database") String database,
                       @Param("schema") String schema,
                       @Param("tableName") String tableName);

    List<String> fetchTableName(@Param("database") String database,
                                @Param("schema") String schema,
                                @Param("tableNamePrefix") String tableNamePrefix);

    int createSyncVersionRecordTable(@Param("database") String database,
                                     @Param("schema") String schema,
                                     @Param("syncVersionRecordTable") String syncVersionRecordTable);

    List<UpdateVersion> getUnsuccessfulVersions(@Param("syncVersionRecordTable") String syncVersionRecordTable);

    int updateUnsuccessfulVersions(@Param("syncVersionRecordTable") String syncVersionRecordTabl);

    UpdateVersion getSuccessUpdateVersion(@Param("syncVersionRecordTable") String syncVersionRecordTable);

    UpdateVersion getUpdateVersionByVersion(@Param("syncVersionRecordTable") String syncVersionRecordTable,
                                            @Param("version") long version);

    int insertUpdateVersion(@Param("syncVersionRecordTable") String syncVersionRecordTable,
                            @Param("updateVersion") UpdateVersion updateVersion);

    int updateSyncVersionRecord(@Param("syncVersionRecordTable") String syncVersionRecordTable,
                                @Param("updateVersion") UpdateVersion updateVersion);

    int createIncrementalView(@Param("lastVersion") Long lastVersion,
                              @Param("currentVersion") Long currentVersion,
                              @Param("versionViewName") String versionViewName,
                              @Param("syncPrimaryVersionTableName") String syncPrimaryVersionTableName,
                              @Param("force") boolean force);

    int dropIncrementalView(@Param("versionViewName") String versionViewName);

    List<Map<String, Object>> traverseIncrementalView(@Param("versionViewName") String versionViewName,
                                                      @Param("limit") int limit,
                                                      @Param("offset") int offset);

    List<Map<String, Object>> queryData(@Param("sql") String sql);

    int updateSyncVersion(@Param("database") String database,
                          @Param("schema") String schema,
                          @Param("syncPrimaryVersionTableName") String syncPrimaryVersionTableName,
                          @Param("ids") Collection<String> ids,
                          @Param("version") long version,
                          @Param("force") boolean force);

    int deleteSyncVersion(@Param("database") String database,
                          @Param("schema") String schema,
                          @Param("syncPrimaryVersionTableName") String syncPrimaryVersionTableName,
                          @Param("ids") List<String> ids);

    int countSyncNumber(@Param("versionViewName") String versionViewName);

    int countSyncVersionWhichLeft(@Param("database") String database,
                                  @Param("schema") String schema,
                                  @Param("syncPrimaryVersionTableName") String syncPrimaryVersionTableName,
                                  @Param("primaryTable") String primaryTable,
                                  @Param("id") String id);

    int insertSyncVersionWhichLeft(@Param("database") String database,
                                   @Param("schema") String schema,
                                   @Param("syncPrimaryVersionTableName") String syncPrimaryVersionTableName,
                                   @Param("primaryTable") String primaryTable,
                                   @Param("id") String id);
}
