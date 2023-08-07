package com.dbapp.extension.ai.job;

import com.dbapp.extension.ai.api.service.IBigdataService;
import com.dbapp.extension.ai.management.AIModelManager;
import com.dbapp.extension.mirror.dto.AIModel;
import com.dbapp.job.core.enums.EditTypeEnum;
import com.dbapp.job.core.enums.ScheduleTypeEnum;
import com.dbapp.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PollingAIModelJob {

    @Resource
    private IBigdataService iBigdataService;
    @Resource
    private AIModelManager aiModelProcessCache;

    @XxlJob(value = "ai-server-polling-job",
            name = "ai服务定时拉取模型任务",
            desc = "ai服务定时拉取模型，并更新现有ai模型任务",
            scheduleType = ScheduleTypeEnum.FIX_RATE,
            scheduleConf = "600",
            editType = EditTypeEnum.NONE,
            otherKey = "ai-server-polling-job",
            autoRegistry = false)
    public void execute() {
        try {
            List<AIModel> aiModels = iBigdataService.getAIModelList()
                    .parallelStream()
                    .filter(AIModel::isEvent)
                    .collect(Collectors.toList());
            aiModels.forEach(aiModel -> aiModelProcessCache.computeIfChange(aiModel));// 存入缓存
            // 调整缓存并重新调整任务调度
            aiModelProcessCache.adjustScheduler();
        } catch (Exception e) {
            log.error("轮询AI模型任务异常", e);
        }
    }
}