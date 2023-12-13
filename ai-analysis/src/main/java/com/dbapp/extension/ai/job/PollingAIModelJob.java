package com.dbapp.extension.ai.job;

import com.dbapp.extension.ai.api.service.IBigdataService;
import com.dbapp.extension.ai.management.AIModelManager;
import com.dbapp.extension.mirror.dto.AIModel;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.enums.EditTypeEnum;
import com.xxl.job.core.enums.ScheduleTypeEnum;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
            XxlJobHelper.log("开始查询ai模型。。。");
            List<AIModel> aiModels = iBigdataService.getAIModelList()
                    .parallelStream()
                    .filter(AIModel::isEvent)
                    .collect(Collectors.toList());
            aiModels.forEach(aiModel -> aiModelProcessCache.computeIfChange(aiModel));// 存入缓存
            // 调整缓存并重新调整任务调度
            XxlJobHelper.log("查到开启的ai模型{}条，判断并调整执行中的任务", aiModels.size());
            aiModelProcessCache.adjustScheduler();
            XxlJobHelper.log("调整结束，任务完成");
        } catch (Exception e) {
            XxlJobHelper.log("轮询AI模型任务异常");
            XxlJobHelper.log(e);
        }
    }
}