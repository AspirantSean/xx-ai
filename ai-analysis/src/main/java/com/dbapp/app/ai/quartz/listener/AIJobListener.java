package com.dbapp.app.ai.quartz.listener;

import com.dbapp.app.ai.management.runtime.process.AIModelProcess;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.quartz.*;

import java.text.SimpleDateFormat;

@Slf4j
public class AIJobListener implements JobListener {

    private String name;

    public AIJobListener(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * 任务运行前
     *
     * @param jobExecutionContext
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        // 获取任务数据
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long startTime = System.currentTimeMillis();
        log.info("任务 {}-{} 开始运行，开始时间：{}", jobDetail.getKey().getGroup(), jobDetail.getKey().getName(), sdf.format(startTime));
        jobDataMap.put("startTime", startTime);
    }

    /**
     * 任务被终止
     *
     * @param jobExecutionContext
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        log.info("任务 {}-{} 被终止", jobDetail.getKey().getGroup(), jobDetail.getKey().getName());
    }

    /**
     * 任务运行后
     *
     * @param jobExecutionContext
     * @param e
     */
    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        AIModelProcess aiModelProcess = (AIModelProcess) jobDataMap.get("aiModelProcess");
        if (aiModelProcess != null) {// AI模型
            String isDead = aiModelProcess.destroy() ? "已销毁" : "未销毁";
            log.info("AI模型 {}-{} {}", aiModelProcess.getJobGroupName(), aiModelProcess.getJobName(), isDead);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long startTime = (long) jobDataMap.remove("startTime");
        long endTime = System.currentTimeMillis();
        log.info("任务 {}-{} 运行结束，结束时间：{}，耗时：{}s",
                jobDetail.getKey().getGroup(), jobDetail.getKey().getName(), sdf.format(endTime), (endTime - startTime) / 1000);
        if (e != null && StringUtils.isNotBlank(e.getMessage())) {
            log.error(String.format("任务 %s-%s 运行异常", jobDetail.getKey().getGroup(), jobDetail.getKey().getName()), e);
        }
    }
}
