package com.dbapp.app.ai.job;

import com.dbapp.app.ai.api.service.IBigdataService;
import com.dbapp.app.ai.management.AIModelManager;
import com.dbapp.app.mirror.dto.AIModel;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@DisallowConcurrentExecution
public class PollingAIModelJob implements Job {

    @Resource
    private IBigdataService iBigdataService;
    @Resource
    private AIModelManager aiModelProcessCache;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
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