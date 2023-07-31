package com.dbapp.extension.sync.core;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.dbapp.extension.sync.constant.Constant;
import com.dbapp.extension.sync.core.synchronize.Synchronizer;
import com.dbapp.extension.sync.enums.SyncStatus;
import com.dbapp.extension.sync.mapper.SynchronizerMapper;
import com.dbapp.extension.sync.model.ao.DatabaseConfig;
import com.dbapp.extension.sync.model.ao.DatasourceDefinition;
import com.dbapp.extension.sync.model.ao.SyncTableConfig;
import com.dbapp.extension.sync.model.ao.SyncTableIDefinition;
import com.dbapp.extension.sync.model.dto.UpdateVersion;
import com.dbapp.extension.sync.prototype.es.IEsService;
import com.dbapp.extension.sync.util.GlobalAttribute;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple3;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Order(1)
public class Initialization implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private DatasourceDefinition datasourceDefinition;
    @Resource
    private IEsService iEsService;
    @Resource
    private SynchronizerMapper synchronizerMapper;
    @Resource
    private Synchronizer synchronizer;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        DatabaseConfig databaseConfig = datasourceDefinition.getDatabaseConfig();
        List<SyncTableIDefinition> syncTableIDefinitions = datasourceDefinition.getSyncTableIDefinitions();
        if (CollUtil.isNotEmpty(syncTableIDefinitions)) {
            String database = databaseConfig.getDatabase();
            String schema = "public";
            String index = databaseConfig.getIndex();
            boolean hasNotCreatedTemplate = true;
            for (SyncTableIDefinition syncTableIDefinition : syncTableIDefinitions) {
                // 基本信息
                SyncTableConfig syncTableConfig = syncTableIDefinition.getSyncTableConfig();
                schema = syncTableConfig.getSchema();
                // 初始化es template或者mapping
                Map<String, Object> template = syncTableIDefinition.getTemplate();
                if (hasNotCreatedTemplate && CollUtil.isNotEmpty(template)) {
                    try {
                        AcknowledgedResponse acknowledgedResponse = iEsService.putTemplate(new PutIndexTemplateRequest(index).source(template));
                        if (acknowledgedResponse.isAcknowledged()) {
                            hasNotCreatedTemplate = false;
                            log.info("创建elasticsearch template成功");
                        } else {
                            log.error("创建elasticsearch template失败，reason：{}；template：{}", acknowledgedResponse, JSON.toJSONString(template));
                        }
                    } catch (IOException e) {
                        log.error("创建elasticsearch template失败，template：" + JSON.toJSONString(template), e);
                    }
                }
                // 初始化pg version表
                String versionTableName = Constant.syncVersionTableNameGetter.apply(syncTableConfig.getTable());
                if (!synchronizerMapper.existTable(database, schema, versionTableName)) {
                    synchronizerMapper.createVersionTable(database, syncTableConfig.getSchema(), versionTableName);
                }
                syncTableIDefinition.setVersionTableName(versionTableName);
                // 创建触发器：业务数据变更 -> 触发pg version表version变更 -> 触发pg version表sync_type变更
                List<Tuple3<String, String, String>> obtainRelatedTables = syncTableConfig.obtainRelatedTables();
                // 最外层主表变更
                Tuple3<String, String, String> firstTable = obtainRelatedTables.remove(0);
                if (!synchronizerMapper.existTrigger("notify_" + firstTable.getT1() + "_trigger")) {
                    synchronizerMapper.createFirstTrigger(database,
                            schema,
                            "notify_" + firstTable.getT1(),
                            versionTableName,
                            firstTable.getT2(),
                            firstTable.getT1());
                }
                for (Tuple3<String, String, String> obtainRelatedTable : obtainRelatedTables) {
                    if (synchronizerMapper.existTrigger("notify_" + obtainRelatedTable.getT1() + "_trigger")) {
                        continue;
                    }
                    if (StringUtils.isBlank(obtainRelatedTable.getT3())) {
                        // 直接影响pg version表
                        synchronizerMapper.createDirectEffectTrigger(database,
                                schema,
                                "notify_" + obtainRelatedTable.getT1(),
                                versionTableName,
                                obtainRelatedTable.getT2(),
                                obtainRelatedTable.getT1());
                    } else {
                        // 间接影响pg version表
                        synchronizerMapper.createIndirectEffectTrigger(database,
                                schema,
                                "notify_" + obtainRelatedTable.getT1(),
                                versionTableName,
                                obtainRelatedTable.getT2(),
                                obtainRelatedTable.getT1(),
                                obtainRelatedTable.getT3());
                    }
                }
                // 删除历史遗留的version view
                List<String> viewNames = synchronizerMapper.fetchTableName(database, schema, Constant.VERSION_VIEW_NAME + "%");
                for (String viewName : viewNames) {
                    synchronizerMapper.dropIncrementalView(database + "." + schema + "." + viewName);
                }
                // 每次启动后做数据一致性校验
                if (synchronizerMapper.countSyncVersionWhichLeft(database, schema, versionTableName, syncTableConfig.getTable(), syncTableConfig.getKey()) > 0) {
                    int total = synchronizerMapper.insertSyncVersionWhichLeft(database, schema, versionTableName, syncTableConfig.getTable(), syncTableConfig.getKey());
                    log.info("同步" + total + "条数据至版本表");
                }
            }
            // 判断已同步版本记录表是否存在
            if (!synchronizerMapper.existTable(database, schema, Constant.syncVersionRecordTableNameGetter.apply(index))) {
                // 如果不存在则新建
                synchronizerMapper.createSyncVersionRecordTable(database, schema, Constant.syncVersionRecordTableNameGetter.apply(index));
            }
            datasourceDefinition.setSyncVersionRecordTable(database + "." + schema + "." + Constant.syncVersionRecordTableNameGetter.apply(index));
            // 1、开启全量同步；2、遍历历史同步版本号继续完成历史失败版本同步
            if ("true".equals(GlobalAttribute.getPropertyString("full.synchronize", "true"))) {
                // 全量同步
                UpdateVersion updateVersion = synchronizer.fullSynchronization(true);
                // 清理所有历史失败条目
                updateVersion = synchronizerMapper.getUpdateVersionByVersion(datasourceDefinition.getSyncVersionRecordTable(), updateVersion.getVersion());
                if (SyncStatus.Success == updateVersion.getStatus())
                    synchronizerMapper.updateUnsuccessfulVersions(datasourceDefinition.getSyncVersionRecordTable());
            } else {
                // 查询是否需要更新：1、历史更新未成功的；2、最新时间范围内需更新的
                List<UpdateVersion> unsuccessfulVersions = synchronizerMapper.getUnsuccessfulVersions(datasourceDefinition.getSyncVersionRecordTable());
                for (UpdateVersion unsuccessfulVersion : unsuccessfulVersions) {
                    // 重新更新
                    synchronizer.incrementalSynchronizationByVersion(unsuccessfulVersion);
                }
                UpdateVersion successUpdateVersion = synchronizerMapper.getSuccessUpdateVersion(datasourceDefinition.getSyncVersionRecordTable());
                UpdateVersion currentUpdateVersion = UpdateVersion.builder()
                        .version(System.currentTimeMillis())
                        .lastVersion(successUpdateVersion == null ? 0 : successUpdateVersion.getVersion())
                        .build();
                // 更新最新
                synchronizer.incrementalSynchronizationByVersion(currentUpdateVersion);
            }
        }
    }
}
