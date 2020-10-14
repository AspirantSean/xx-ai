package com.dbapp.app.ai.management;

import com.dbapp.app.ai.job.AIModelAnalysisJob;
import com.dbapp.app.ai.job.PollingAIModelJob;
import com.dbapp.app.ai.management.runtime.process.AIModelProcess;
import com.dbapp.app.ai.mapper.AIAnomalyAnalysisMapper;
import com.dbapp.app.ai.quartz.IQuartzManager;
import com.dbapp.app.ai.utils.GlobalAttribute;
import com.dbapp.app.mirror.dto.AIModel;
import com.google.common.collect.ImmutableMap;
import com.sun.istack.internal.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.quartz.DateBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public final class AIModelManager {

    private static final String POLLING_JOB_NAME = "polling.job.name";
    private static final String POLLING_JOB_GROUP_NAME = "polling.job.group.name";
    private static final String POLLING_TRIGGER_NAME = "polling.trigger.name";
    private static final String POLLING_TRIGGER_GROUP_NAME = "polling.trigger.group.name";

    @Resource
    private IQuartzManager modelQuartz;
    @Resource
    private AIAnomalyAnalysisMapper aiAnomalyAnalysisMapper;

    /**
     * 初始化任务
     * 定时调整轮询AI模型的时间间隔
     */
    @PostConstruct
    private void initializeTask() {
        new Timer().schedule(new TimerTask() {
            private final Map<String, Object> cache = new ConcurrentHashMap<>();

            @Override
            public void run() {
                int interval = GlobalAttribute.getPropertyInteger("ai.model.polling.interval", 10);
                String intervalUnit = GlobalAttribute.getPropertyString("ai.model.polling.unit", "MINUTE");
                if (Objects.equals(interval, cache.get("interval"))
                        && Objects.equals(intervalUnit, cache.get("intervalUnit"))) {// 若未修改则不变
                    return;
                } else {// 若修改则更新缓存配置
                    cache.put("interval", interval);
                    cache.put("intervalUnit", intervalUnit);
                }
                if (!modelQuartz.listTriggers(GroupMatcher.groupContains("polling.trigger")).isEmpty()) {// 若已经存在任务则移除
                    modelQuartz.removeJob(POLLING_JOB_NAME, POLLING_JOB_GROUP_NAME, POLLING_TRIGGER_NAME, POLLING_TRIGGER_GROUP_NAME);
                }
                modelQuartz.addJob(
                        POLLING_JOB_NAME, POLLING_JOB_GROUP_NAME, POLLING_TRIGGER_NAME, POLLING_TRIGGER_GROUP_NAME,
                        PollingAIModelJob.class,
                        interval,
                        DateBuilder.IntervalUnit.valueOf(intervalUnit));
            }
        }, 5000, 10000);// 每10秒检测一次时间间隔配置是否修改
    }

    /**
     * 当前运行中的模型
     */
    private final Map<String, AIModelProcess> runtimeAiModelProcessCache = new ConcurrentHashMap<>();
    /**
     * 无需更改的运行中模型
     */
    private final Map<String, AIModelProcess> notChangedRuntimeAiModelProcessCache = new ConcurrentHashMap<>();
    /**
     * 需要移除的运行中的模型（删除的模型、修改过的模型都将移除，其中修改过的模型将加入新增模型列表）
     */
    private final Map<String, AIModelProcess> removeRuntimeAiModelProcessCache = new ConcurrentHashMap<>();
    /**
     * 新增的模型
     */
    private final Map<String, AIModelProcess> newAiModelProcessCache = new ConcurrentHashMap<>();

    /**
     * 若AIModel不存在则新增；
     * 若AIModel存在且无修改则不动；
     * 若AIModel存在且被修改则移除原有进程，加入新增列表；
     *
     * @param aiModel 模型
     * @return 是否修改修改：true-修改；false-无需修改
     */
    public synchronized boolean computeIfChange(@NotNull AIModel aiModel) {
        if (this.runtimeAiModelProcessCache.containsKey(aiModel.getRuleId())) {// 存在则比较是否需要修改
            AIModelProcess existProcess = this.runtimeAiModelProcessCache.get(aiModel.getRuleId());
            if (existProcess.isChange(aiModel)) {// 修改
                this.removeRuntimeAiModelProcessCache.put(aiModel.getRuleId(), existProcess);
                this.newAiModelProcessCache.put(aiModel.getRuleId(), new AIModelProcess(aiModel));
            } else {// 不修改
                this.notChangedRuntimeAiModelProcessCache.put(aiModel.getRuleId(), existProcess);
                return false;
            }
        } else {// 不存在则新增
            this.newAiModelProcessCache.put(aiModel.getRuleId(), new AIModelProcess(aiModel));
        }
        return true;
    }

    /**
     * 调整任务调度
     */
    public synchronized void adjustScheduler() {
        log.info("轮询AI模型任务：开始调整AI模型任务...");
        adjustCache();
        // 移除任务
        this.removeRuntimeAiModelProcessCache.forEach((ruleId, aiModelProcess) -> {
            aiModelProcess.destroy();
            modelQuartz.removeJob(aiModelProcess.getJobName(), aiModelProcess.getJobGroupName(),
                    aiModelProcess.getTriggerName(), aiModelProcess.getTriggerGroupName());
        });
        if (!this.removeRuntimeAiModelProcessCache.isEmpty()) {
            log.info("轮询AI模型任务：移除AI模型任务-{}", String.join(",", this.removeRuntimeAiModelProcessCache.keySet()));
        }
        // 添加任务
        this.newAiModelProcessCache.forEach(
                (aiId, aiModelProcess) -> modelQuartz.addJob(aiModelProcess.getJobName(), aiModelProcess.getJobGroupName(),
                        aiModelProcess.getTriggerName(), aiModelProcess.getTriggerGroupName(),
                        AIModelAnalysisJob.class, aiModelProcess.getInterval(), aiModelProcess.getIntervalUnit(),
                        ImmutableMap.of("aiModelProcess", aiModelProcess)));
        if (!this.newAiModelProcessCache.isEmpty()) {
            log.info("轮询AI模型任务：新增AI模型任务-{}", String.join(",", this.newAiModelProcessCache.keySet()));
        }
        // 校验是否当前的任务调度与缓存中的一致
        validateExistAIJob();
        // 清理数据库中AI模型修改后的遗留分析数据
        List<String> modelIds = this.runtimeAiModelProcessCache.keySet().stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        int deleteAIData = this.aiAnomalyAnalysisMapper.deleteAIAnalysisDataNotInModelIds(modelIds);
        log.info("轮询AI模型任务：清理AI异常分析数据{}条", deleteAIData);
        // 清理缓存
        this.notChangedRuntimeAiModelProcessCache.clear();
        this.removeRuntimeAiModelProcessCache.clear();
        this.newAiModelProcessCache.clear();
        log.info("轮询AI模型任务：调整AI模型任务完成");
    }

    /**
     * 校验运行中的任务调度与缓存管理中的是否一致
     */
    private synchronized void validateExistAIJob() {
        log.info("轮询AI模型任务：校验运行中的任务调度与缓存管理中的是否一致");
        Set<TriggerKey> triggerKeys = modelQuartz.listTriggers(GroupMatcher.groupContains("AI"));
        Set<String> triggerNames = triggerKeys.stream()
                .map(triggerKey -> {
                    if (this.runtimeAiModelProcessCache.containsKey(triggerKey.getName())) {
                        return triggerKey.getName();
                    }
                    // 清楚不包含在可运行的模型中的已有任务调度
                    this.modelQuartz.removeJob(triggerKey.getName(), triggerKey.getGroup(), triggerKey.getName(), triggerKey.getGroup());
                    return null;
                })
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        List<AIModelProcess> unStartJobList = new ArrayList<>();
        this.runtimeAiModelProcessCache.forEach((ruleId, aiModelProcess) -> {
            if (!triggerNames.contains(aiModelProcess.getTriggerName())) {
                unStartJobList.add(aiModelProcess);
            }
        });
        unStartJobList.forEach(aiModelProcess ->
                this.modelQuartz.addJob(aiModelProcess.getJobName(), aiModelProcess.getJobGroupName(),
                        aiModelProcess.getTriggerName(), aiModelProcess.getTriggerGroupName(),
                        AIModelAnalysisJob.class, aiModelProcess.getInterval(), aiModelProcess.getIntervalUnit(),
                        ImmutableMap.of("aiModelProcess", aiModelProcess)));
    }

    /**
     * 调整缓存信息用于后续任务调度修改
     * <p>每次轮询模型后，开始修改任务调度前：
     * <p>    runtimeAiModelProcessCache = notChangedRuntimeAiModelProcessCache + removeRuntimeAiModelProcessCache
     * <p>每次轮询模型后，开始修改任务调度前：
     * <p>    轮询查到的AI模型 = notChangedRuntimeAiModelProcessCache + newAiModelProcessCache
     * <p>每次轮询模型并修改任务调度后：
     * <p>    runtimeAiModelProcessCache = notChangedRuntimeAiModelProcessCache + newAiModelProcessCache
     *
     * @return
     */
    private synchronized void adjustCache() {
        this.runtimeAiModelProcessCache.forEach((ruleId, aiModelProcess) -> {
            if (!this.notChangedRuntimeAiModelProcessCache.containsKey(ruleId)
                    && !this.removeRuntimeAiModelProcessCache.containsKey(ruleId)) {
                this.removeRuntimeAiModelProcessCache.put(ruleId, aiModelProcess);
            }
        });
        // 将新一轮的运行模型装入缓存
        this.runtimeAiModelProcessCache.clear();
        this.runtimeAiModelProcessCache.putAll(this.notChangedRuntimeAiModelProcessCache);
        this.runtimeAiModelProcessCache.putAll(this.newAiModelProcessCache);
    }

    /**
     * 销毁所有AI模型进程
     */
    public synchronized void destroyAll() {
        if (!this.runtimeAiModelProcessCache.isEmpty()) {
            this.runtimeAiModelProcessCache.values().forEach(AIModelProcess::destroy);
        }
        if (!this.notChangedRuntimeAiModelProcessCache.isEmpty()) {
            this.notChangedRuntimeAiModelProcessCache.values().forEach(AIModelProcess::destroy);
        }
        if (!this.removeRuntimeAiModelProcessCache.isEmpty()) {
            this.removeRuntimeAiModelProcessCache.values().forEach(AIModelProcess::destroy);
        }
    }

}
