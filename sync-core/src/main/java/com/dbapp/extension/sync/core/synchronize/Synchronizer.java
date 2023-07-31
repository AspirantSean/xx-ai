package com.dbapp.extension.sync.core.synchronize;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.dbapp.extension.sync.constant.Constant;
import com.dbapp.extension.sync.enums.SyncStatus;
import com.dbapp.extension.sync.enums.SyncType;
import com.dbapp.extension.sync.mapper.SynchronizerMapper;
import com.dbapp.extension.sync.model.ao.DatasourceDefinition;
import com.dbapp.extension.sync.model.ao.Mapping;
import com.dbapp.extension.sync.model.ao.SyncTableConfig;
import com.dbapp.extension.sync.model.ao.SyncTableIDefinition;
import com.dbapp.extension.sync.model.dto.UpdateVersion;
import com.dbapp.extension.sync.prototype.es.IEsService;
import com.dbapp.extension.sync.util.GlobalAttribute;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.elasticsearch.client.Requests.INDEX_CONTENT_TYPE;

@Slf4j
@Component
public class Synchronizer implements ISynchronizer {

    @Resource
    private IEsService iEsService;
    @Resource
    @Lazy
    private ISynchronizer iSynchronizer;
    @Resource
    private SynchronizerMapper synchronizerMapper;
    @Resource
    private DatasourceDefinition datasourceDefinition;

    /**
     * 线程池，限制在全量同步 分批查询->同步elasticsearch 阶段，避免死锁等待
     */
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            10, 100, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000), new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 主分批线程，优先于同步动作
     */
    private final ThreadPoolExecutor primaryThreadPoolExecutor = new ThreadPoolExecutor(
            10, 100, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000), new ThreadPoolExecutor.CallerRunsPolicy());

    /**
     * 全量同步
     */
    @Override
    public UpdateVersion fullSynchronization(boolean wait, boolean force) {
        // 全量同步版本号设置
        UpdateVersion currentVersion = UpdateVersion.builder()
                .version(System.currentTimeMillis())
                .lastVersion(0)
                .force(force)
                .status(SyncStatus.New)
                .description("开始全量同步任务，请勿重复执行！")
                .build();
        if (wait) {
            try {
                return iSynchronizer.doFullSynchronization(currentVersion).get();
            } catch (Exception e) {
                return currentVersion.clone();
            }
        } else {
            return currentVersion.clone();
        }
    }

    @Async
    @Override
    public Future<UpdateVersion> doFullSynchronization(UpdateVersion currentVersion) {
        incrementalSynchronizationByVersion(currentVersion);
        return new AsyncResult<>(currentVersion.clone());
    }

    @Override
    public void incrementalSynchronizationByVersion(UpdateVersion currentVersion) {
        // 开始时间
        long start = System.currentTimeMillis();
        // 记录版本情况
        currentVersion.setStatus(SyncStatus.New);
        synchronizerMapper.insertUpdateVersion(datasourceDefinition.getSyncVersionRecordTable(), currentVersion);
        // 循环配置
        String database = datasourceDefinition.getDatabaseConfig().getDatabase();
        for (SyncTableIDefinition syncTableIDefinition : datasourceDefinition.getSyncTableIDefinitions()) {
            SyncTableConfig syncTableConfig = syncTableIDefinition.getSyncTableConfig();
            String schema = syncTableConfig.getSchema();
            // 创建视图
            String versionViewName = database + "." + schema + "." + Constant.VERSION_VIEW_NAME + currentVersion.getVersion();
            if (!synchronizerMapper.existTable(database, schema, Constant.VERSION_VIEW_NAME + currentVersion.getVersion())) {
                synchronizerMapper.createIncrementalView(
                        currentVersion.getLastVersion(), currentVersion.getVersion(),
                        versionViewName, database + "." + schema + "." + syncTableIDefinition.getVersionTableName(),
                        currentVersion.isForce());
            }
            // 遍历视图数据
            final AtomicInteger tryTimes = new AtomicInteger(0);
            int batchSize = 30000;
            int leftCount = synchronizerMapper.countSyncNumber(versionViewName);
            // 记录版本情况
            currentVersion.setStatus(SyncStatus.Synchronizing);
            currentVersion.setTotal(leftCount);
            synchronizerMapper.insertUpdateVersion(datasourceDefinition.getSyncVersionRecordTable(), currentVersion);
            while (leftCount > 0) {
                List<String> failedData = syncUseThreadPool(currentVersion,
                        syncTableIDefinition, database, schema, versionViewName, leftCount, batchSize);
                // 判断是否全部成功
                if (!failedData.isEmpty()) {
                    if (tryTimes.incrementAndGet() >= 5) {
                        // 处理重试tryTimes次后依旧失败的数据
                        String message = "重试" + 5 + "次后仍旧失败，elasticsearch批量写入报错。失败条目id：" + String.join("、", failedData) + " 等";
                        currentVersion.setDescription(message);
                        currentVersion.setStatus(SyncStatus.Failed);
                        synchronizerMapper.updateSyncVersionRecord(datasourceDefinition.getSyncVersionRecordTable(), currentVersion);
                        log.error(message);
                        break;
                    } else {
                        currentVersion.setDescription("重试" + tryTimes.get() + "次失败，elasticsearch批量写入报错。失败条目id：" + String.join("、", failedData) + " 等");
                        currentVersion.setStatus(SyncStatus.Synchronizing);
                        synchronizerMapper.updateSyncVersionRecord(datasourceDefinition.getSyncVersionRecordTable(), currentVersion);
                    }
                    continue;
                }
                int left = synchronizerMapper.countSyncNumber(versionViewName);
                if (left >= leftCount) {// 失败
                    if (tryTimes.incrementAndGet() >= 5) {
                        // 处理重试tryTimes次后依旧失败的数据
                        String message = "重试" + tryTimes.get() + "次后仍旧失败，剩余" + left + "条。失败条目id："
                                + synchronizerMapper.traverseIncrementalView(versionViewName, 10, 0)
                                .stream()
                                .map(syncDatum -> String.valueOf(syncDatum.get("id"))).collect(Collectors.joining("、"))
                                + " 等";
                        currentVersion.setDescription(message);
                        currentVersion.setStatus(SyncStatus.Failed);
                        synchronizerMapper.updateSyncVersionRecord(datasourceDefinition.getSyncVersionRecordTable(), currentVersion);
                        log.error(message);
                        break;
                    } else {
                        currentVersion.setDescription("重试" + tryTimes.get() + "次失败，当次同步后待同步资产未减少，剩余" + left + "条");
                        currentVersion.setStatus(SyncStatus.Synchronizing);
                        synchronizerMapper.updateSyncVersionRecord(datasourceDefinition.getSyncVersionRecordTable(), currentVersion);
                    }
                } else {
                    currentVersion.setDescription("本次数据同步成功，剩余" + left + "条");
                    currentVersion.setStatus(SyncStatus.Synchronizing);
                    synchronizerMapper.updateSyncVersionRecord(datasourceDefinition.getSyncVersionRecordTable(), currentVersion);
                }
                leftCount = left;
            }
            // 记录结果结束
            long end = System.currentTimeMillis();
            currentVersion.setTook(end - start);
            if (leftCount == 0) {// 成功
                currentVersion.setDescription("本次数据同步成功");
                currentVersion.setStatus(SyncStatus.Success);
                synchronizerMapper.updateSyncVersionRecord(datasourceDefinition.getSyncVersionRecordTable(), currentVersion);
            } else {// 失败
                currentVersion.setDescription(currentVersion.getDescription() + "；本次数据同步失败，剩余" + leftCount + "条未同步");
                currentVersion.setStatus(SyncStatus.Failed);
                synchronizerMapper.updateSyncVersionRecord(datasourceDefinition.getSyncVersionRecordTable(), currentVersion);
            }
            // 删除视图
            synchronizerMapper.dropIncrementalView(versionViewName);
        }
    }

    private List<String> syncUseThreadPool(UpdateVersion currentVersion,
                                           SyncTableIDefinition syncTableIDefinition,
                                           String database,
                                           String schema,
                                           String versionViewName,
                                           int leftCount,
                                           int batchSize) {
        if ("true".equals(GlobalAttribute.getPropertyString("use.thread.sync", "false"))) {
            // 将数据分线程执行
            List<Future<List<String>>> futureTasks = Stream.iterate(0, index -> index + 1)
                    .limit((leftCount + batchSize - 1) / batchSize)
                    .map(index -> primaryThreadPoolExecutor.submit(
                            () -> doSyncUseThreadPool(currentVersion, syncTableIDefinition, database,
                                    schema, versionViewName, batchSize, index)))
                    .collect(Collectors.toList());
            // 等待结束
            return futureTasks.stream()
                    .map(future -> {
                        try {
                            // 整合失败条目
                            return future.get();
                        } catch (Exception e) {
                            log.error("同步任务执行失败，线程池调度执行异常", e);
                            return new ArrayList<String>();
                        }
                    })
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } else {
            return doSyncUseThreadPool(currentVersion, syncTableIDefinition,
                    database, schema, versionViewName, batchSize, 0);
        }
    }

    private List<String> doSyncUseThreadPool(UpdateVersion currentVersion,
                                             SyncTableIDefinition syncTableIDefinition,
                                             String database,
                                             String schema,
                                             String versionViewName,
                                             int batchSize,
                                             int index) {
        List<Map<String, Object>> syncData = synchronizerMapper.traverseIncrementalView(versionViewName, batchSize, index * batchSize);
        if (syncData.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Boolean, List<Map<String, Object>>> syncDataMap = syncData.stream()
                .collect(Collectors.partitioningBy(syncDatum -> SyncType.delete.name().equals(syncDatum.get("syncType"))));
        // 失败条目
        List<String> failedData = new ArrayList<>();
        // 任务结果
        List<Future<List<String>>> futures = new ArrayList<>();
        // 删除不存在的资产
        List<String> deleteIds = syncDataMap.get(Boolean.TRUE)
                .stream()
                .map(syncDatum -> String.valueOf(syncDatum.get("id")))
                .collect(Collectors.toList());
        if (!deleteIds.isEmpty()) {
            // 切分成3000一个线程执行删除同步
            futures.addAll(Lists.partition(deleteIds, 3000)
                    .stream()
                    .map(ArrayList::new)
                    .map(deleteIdList -> threadPoolExecutor.submit(() -> {
                        List<String> failedDeleteIds = deleteSynchronizeData(deleteIdList);
                        // 修改视图中删除成功条目数据的version
                        if (!failedDeleteIds.isEmpty()) {
                            // 移除失败条目
                            deleteIdList.removeAll(failedDeleteIds);
                        }
                        if (!deleteIdList.isEmpty()) {
                            // 修改成功条目
                            synchronizerMapper.deleteSyncVersion(database, schema, syncTableIDefinition.getVersionTableName(), deleteIdList);
                        }
                        return failedDeleteIds;
                    }))
                    .collect(Collectors.toList()));
        }
        // 待同步数据
        List<Map<String, Object>> syncList = syncDataMap.get(Boolean.FALSE);
        if (!syncList.isEmpty()) {
            // 切分成3000一个线程进行同步
            futures.addAll(Lists.partition(syncList, 3000)
                    .stream()
                    .map(syncVersionList -> threadPoolExecutor.submit(() -> {
                        // 同步数据的id列表
                        List<String> syncIdList = syncVersionList.stream()
                                .map(syncDatum -> String.valueOf(syncDatum.get("id")))
                                .collect(Collectors.toList());
                        // 查询业务数据 数据写入es
                        List<String> failedSyncIds = incrementalSynchronizationByObject(Lists.partition(syncIdList, 300)
                                .stream()
                                .map(subSyncIdList -> queryDataByIds(syncTableIDefinition, subSyncIdList))
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()));
                        // 修改视图中同步成功条目数据的version
                        if (!failedSyncIds.isEmpty()) {
                            // 移除失败条目
                            syncIdList.removeAll(failedSyncIds);
                        }
                        if (!syncIdList.isEmpty()) {
                            // 修改成功条目
                            synchronizerMapper.updateSyncVersion(
                                    database, schema, syncTableIDefinition.getVersionTableName(),
                                    syncIdList, currentVersion.getVersion(), currentVersion.isForce());
                        }
                        return failedSyncIds;
                    }))
                    .collect(Collectors.toList()));
        }
        // 等待结束
        futures.forEach(future -> {
            try {
                // 整合失败条目
                failedData.addAll(future.get());
            } catch (Exception e) {
                log.error("同步任务执行失败，线程池调度执行异常", e);
            }
        });
        return failedData;
    }

    private List<Map<String, Object>> queryDataByIds(SyncTableIDefinition syncTableIDefinition, List<String> ids) {
        SyncTableConfig syncTableConfig = syncTableIDefinition.getSyncTableConfig();
        Mapping mapping = syncTableIDefinition.getMapping();
        // 查询业务数据
        String sql = syncTableIDefinition.getSql() + " WHERE " + syncTableConfig.getAliasWithQuotation() + "." + syncTableConfig.getKey() + " IN (" + ids.stream().collect(Collectors.joining("', '", "'", "'")) + ")";
        // 转化数据为对象
        return mapping.mapping(synchronizerMapper.queryData(sql));
    }

    /**
     * 根据id列表增量同步到elasticsearch
     *
     * @param syncType
     * @param ids
     * @return
     */
    @Override
    public List<String> incrementalSynchronizationById(SyncType syncType, List<String> ids) {
        // 结果集
        List<String> failedData = new ArrayList<>();
        if (CollUtil.isEmpty(ids)) {
            return failedData;
        }
        // 循环配置
        for (SyncTableIDefinition syncTableIDefinition : datasourceDefinition.getSyncTableIDefinitions()) {
            List<Map<String, Object>> data = new ArrayList<>();
            if (SyncType.delete == syncType) {
                // 删除
                failedData.addAll(deleteSynchronizeData(ids));
            } else {
                for (List<String> idList : Lists.partition(ids, 300)) {
                    // 转化数据为对象
                    data.addAll(queryDataByIds(syncTableIDefinition, idList));
                    if (data.size() >= 3000) {
                        failedData.addAll(incrementalSynchronizationByObject(data));
                        data.clear();
                    }
                }
                if (!data.isEmpty()) {
                    failedData.addAll(incrementalSynchronizationByObject(data));
                    data.clear();
                }
            }
        }
        return failedData;
    }

    @Override
    public List<String> deleteSynchronizeData(List<String> ids) {
        String index = datasourceDefinition.getDatabaseConfig().getIndex();
        List<String> failedData = new ArrayList<>();
        for (List<String> partIds : Lists.partition(ids, 3000)) {
            try {
                BulkResponse bulkResponse = iEsService.bulk(new BulkRequest()
                        .add(partIds.stream()
                                .map(id -> new DeleteRequest()
                                        .index(index)
                                        .id(id))
                                .collect(Collectors.toList())));
                if (bulkResponse.hasFailures()) {
                    List<String> failedIds = Arrays.stream(bulkResponse.getItems())
                            .filter(BulkItemResponse::isFailed)
                            .map(BulkItemResponse::getId)
                            .collect(Collectors.toList());
                    failedData.addAll(failedIds);
                }
            } catch (IOException e) {
                failedData.addAll(partIds);
                log.error("数据批量删除elasticsearch失败", e);
            }
        }
        return failedData;
    }

    /**
     * 增量同步实体对象到elasticsearch
     *
     * @param data
     * @return
     */
    @Override
    public List<String> incrementalSynchronizationByObject(List<Map<String, Object>> data) {
        String index = datasourceDefinition.getDatabaseConfig().getIndex();
        String idName = datasourceDefinition.getSyncTableIDefinitions().get(0).getMapping().getKeyAlias();
        List<String> failedData = new ArrayList<>();
        for (List<Map<String, Object>> partData : Lists.partition(data, 3000)) {
            try {
                BulkResponse bulkResponse = iEsService.bulk(new BulkRequest()
                        .add(partData.stream()
                                .map(datum -> new IndexRequest()
                                        .index(index)
                                        .id(String.valueOf(datum.get(idName)))
                                        .source(JSON.toJSONString(datum), INDEX_CONTENT_TYPE))
                                .collect(Collectors.toList())));
                if (bulkResponse.hasFailures()) {
                    List<String> failedIds = Arrays.stream(bulkResponse.getItems())
                            .filter(BulkItemResponse::isFailed)
                            .map(BulkItemResponse::getId)
                            .collect(Collectors.toList());
                    failedData.addAll(failedIds);
                }
            } catch (IOException e) {
                failedData.addAll(partData.stream().map(datum -> String.valueOf(datum.get(idName))).collect(Collectors.toList()));
                log.error("数据批量写入elasticsearch失败", e);
            }
        }
        return failedData;
    }

    @Slf4j
    public static class BulkListener implements ActionListener<BulkResponse> {
        @Override
        public void onResponse(BulkResponse bulkResponse) {
            log.info("批量写入elasticsearch结果：" + bulkResponse.buildFailureMessage());
        }

        @Override
        public void onFailure(Exception e) {
            log.error("批量写入elasticsearch失败", e);
        }
    }

}
