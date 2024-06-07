package com.dbapp.extension.ai.management;

import cn.hutool.core.collection.CollUtil;
import com.dbapp.extension.ai.job.JobService;
import com.dbapp.extension.ai.management.runtime.process.AIModelProcess;
import com.dbapp.extension.ai.mapper.AIAnomalyAnalysisMapper;
import com.dbapp.extension.ai.utils.GlobalAttribute;
import com.dbapp.extension.mirror.dto.AIModel;
import com.xxl.job.core.biz.model.JobInfoResult;
import com.xxl.job.core.biz.model.RegistryJobParam;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.enums.EditTypeEnum;
import com.xxl.job.core.enums.MisfireStrategyEnum;
import com.xxl.job.core.enums.ScheduleTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public final class AIModelManager implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private JobService jobService;
    @Resource
    private AIAnomalyAnalysisMapper aiAnomalyAnalysisMapper;

    private final Map<String, Object> configCache = new ConcurrentHashMap<>();

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            // 启动时清理任务重新加载
            jobService.deleteByHandler("ai-server-executor-job");
            initializeJob();
        } catch (Exception e) {
            log.error("启动时清理任务重新加载异常", e);
            System.exit(0);
        }
    }

    /**
     * 初始化任务
     * 定时调整轮询AI模型的时间间隔
     */
    public void initializeJob() {
        try {
            int interval = GlobalAttribute.getPropertyInteger("ai.model.polling.interval", 10);
            String intervalUnit = GlobalAttribute.getPropertyString("ai.model.polling.unit", "MINUTE");
            if (Objects.equals(interval, configCache.get("interval"))
                    && Objects.equals(intervalUnit, configCache.get("intervalUnit"))) {// 若未修改则不变
                log.info("配置未修改无需重新调整拉取ai模型任务");
                return;
            } else {// 若修改则更新缓存配置
                configCache.put("interval", interval);
                configCache.put("intervalUnit", intervalUnit);
            }
            jobService.deleteByOtherKey("ai-server-polling-job");
            RegistryJobParam jobInfo = new RegistryJobParam();
            jobInfo.setJobName("ai服务定时拉取模型任务");
            jobInfo.setExecutorHandler("ai-server-polling-job");
            jobInfo.setJobDesc("ai服务定时拉取模型，并更新现有ai模型任务");
            jobInfo.setScheduleType(ScheduleTypeEnum.FIX_RATE.name());
            jobInfo.setMisfireStrategy(MisfireStrategyEnum.FIRE_ONCE_NOW.name());
            jobInfo.setScheduleConf(IntervalUnit.valueOf(intervalUnit).translateToSecond(interval) + "");
            jobInfo.setTriggerStatus(1);
            jobInfo.setType(EditTypeEnum.NONE.getCode());
            jobInfo.setManual(true);
            jobInfo.setOtherKey("ai-server-polling-job");
            jobService.addJob(jobInfo);
            executeJobNow("ai-server-polling-job");
            log.info("加入ai服务定时拉取模型任务成功，并手动调用一次");
        } catch (Exception e) {
            log.error("调整ai服务定时拉取模型任务异常", e);
        }
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

    public synchronized AIModelProcess getRunningAiModelProcess(String jobKey) {
        return runtimeAiModelProcessCache.get(jobKey);
    }

    /**
     * 调整任务调度
     */
    public synchronized void adjustScheduler() {
        XxlJobHelper.log("轮询AI模型任务：开始调整AI模型任务...");
        adjustCache();
        // 移除任务
        // 先查到所有任务
        final Map<String, JobInfoResult> jobInfoList = Optional.ofNullable(jobService.getJobsByHandler("ai-server-executor-job"))
                .filter(CollUtil::isNotEmpty)
                .orElseGet(ArrayList::new)
                .stream()
                .collect(Collectors.toMap(JobInfoResult::getOtherKey, jobInfo -> jobInfo));
        this.removeRuntimeAiModelProcessCache.forEach((ruleId, aiModelProcess) -> {
            aiModelProcess.destroy();
            JobInfoResult jobInfo = jobInfoList.get(aiModelProcess.getJobKey());
            jobService.deleteByOtherKey(jobInfo.getOtherKey());
        });
        if (!this.removeRuntimeAiModelProcessCache.isEmpty()) {
            XxlJobHelper.log("轮询AI模型任务：移除AI模型任务-{}", String.join(",", this.removeRuntimeAiModelProcessCache.keySet()));
        }
        // 添加任务
        this.newAiModelProcessCache.forEach((aiId, aiModelProcess) -> addAiModelJob(aiModelProcess));
        if (!this.newAiModelProcessCache.isEmpty()) {
            XxlJobHelper.log("轮询AI模型任务：新增AI模型任务-{}", String.join(",", this.newAiModelProcessCache.keySet()));
        }
        // 校验是否当前的任务调度与缓存中的一致
        validateExistAIJob();
        // 清理数据库中AI模型修改后的遗留分析数据
        List<String> modelIds = this.runtimeAiModelProcessCache.keySet().stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        int deleteAIData = this.aiAnomalyAnalysisMapper.deleteAIAnalysisDataNotInModelIds(modelIds);
        XxlJobHelper.log("轮询AI模型任务：清理AI异常分析数据{}条", deleteAIData);
        // 清理缓存
        this.notChangedRuntimeAiModelProcessCache.clear();
        this.removeRuntimeAiModelProcessCache.clear();
        this.newAiModelProcessCache.clear();
        XxlJobHelper.log("轮询AI模型任务：调整AI模型任务完成");
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
     * 校验运行中的任务调度与缓存管理中的是否一致
     */
    private synchronized void validateExistAIJob() {
        XxlJobHelper.log("轮询AI模型任务：校验运行中的任务调度与缓存管理中的是否一致");
        // 先查到所有任务
        List<String> runningJobs = Optional.ofNullable(jobService.getJobsByHandler("ai-server-executor-job"))
                .filter(CollUtil::isNotEmpty)
                .orElseGet(ArrayList::new)
                .stream()
                .map(jobInfo -> {
                    if (this.runtimeAiModelProcessCache.containsKey(jobInfo.getOtherKey())) {
                        return jobInfo.getOtherKey();
                    }
                    // 清除不包含在可运行的模型中的已有任务调度
                    jobService.deleteByOtherKey(jobInfo.getOtherKey());
                    return null;
                })
                .collect(Collectors.toList());
        for (Map.Entry<String, AIModelProcess> entry : this.runtimeAiModelProcessCache.entrySet()) {
            if (!runningJobs.contains(entry.getKey())) {// 未启动
                AIModelProcess aiModelProcess = entry.getValue();
                addAiModelJob(aiModelProcess);
            }
        }
    }

    private void addAiModelJob(AIModelProcess aiModelProcess) {
        RegistryJobParam jobInfo = new RegistryJobParam();
        jobInfo.setJobName("ai服务模型定时任务");
        jobInfo.setExecutorHandler("ai-server-executor-job");
        jobInfo.setJobDesc("ai服务定时执行ai模型");
        jobInfo.setScheduleType(ScheduleTypeEnum.FIX_RATE.name());
        jobInfo.setMisfireStrategy(MisfireStrategyEnum.FIRE_ONCE_NOW.name());
        jobInfo.setScheduleConf(aiModelProcess.getIntervalUnit().translateToSecond(aiModelProcess.getInterval()) + "");
        jobInfo.setTriggerStatus(1);
        jobInfo.setType(EditTypeEnum.NONE.getCode());
        jobInfo.setManual(true);
        jobInfo.setOtherKey(aiModelProcess.getJobKey());
        jobInfo.setExecutorParam(aiModelProcess.getJobKey());
        jobService.addJob(jobInfo);
        boolean success = executeJobNow(aiModelProcess.getJobKey());
        XxlJobHelper.log("增加任务-{}，并手动执行{}", jobInfo.getOtherKey(), success ? "成功" : "失败");
    }

    private boolean executeJobNow(String otherKey) {
        JobInfoResult jobInfo = jobService.getJobByOtherKey(otherKey);
        if (jobInfo == null) {
            XxlJobHelper.log("当前任务不存在，无法立即执行，otherKey = {}", otherKey);
            return false;
        }
        // 启动一次任务
        return jobService.trigger(jobInfo.getId());
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

    public enum IntervalUnit {
        SECOND(1),
        MINUTE(60),
        HOUR(60 * 60),
        DAY(24 * 60 * 60),
        WEEK(7 * 24 * 60 * 60),
        MONTH(30 * 24 * 60 * 60),
        YEAR(365 * 24 * 60 * 60);

        private final int unit;

        private IntervalUnit(int unit) {
            this.unit = unit;
        }

        public long translateToSecond(long interval) {
            return unit * interval;
        }
    }

}
