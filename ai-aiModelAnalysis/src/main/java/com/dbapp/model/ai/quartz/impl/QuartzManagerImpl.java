package com.dbapp.model.ai.quartz.impl;

import com.dbapp.model.ai.quartz.IQuartzManager;
import com.dbapp.model.ai.quartz.listener.AIJobListener;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.matchers.KeyMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class QuartzManagerImpl implements IQuartzManager {
    private static final Logger LOG = LoggerFactory.getLogger(QuartzManagerImpl.class);

    // 获得 Quartz 的调度器实例 scheduler；
    @Resource(name = "Scheduler")
    private Scheduler scheduler;

    @Override
    public void addJob(String jobName,
                       String jobGroup,
                       String triggerName,
                       String triggerGroup,
                       Class<? extends Job> jobClass,
                       int timeInterval,
                       DateBuilder.IntervalUnit unit) {
        addJob(jobName, jobGroup, triggerName, triggerGroup, jobClass, timeInterval, unit, null);
    }

    @Override
    public void addJob(String jobName,
                       String jobGroup,
                       String triggerName,
                       String triggerGroup,
                       Class<? extends Job> jobClass,
                       int timeInterval,
                       DateBuilder.IntervalUnit unit,
                       Map<String, Object> data) {
        try {
            // 获取 Quartz 任务类实例 jobDetail；
            JobBuilder jobBuilder = JobBuilder.newJob(jobClass)
                    .withIdentity(jobName, jobGroup);
            if (data != null && !data.isEmpty()) {
                jobBuilder.setJobData(new JobDataMap(data));
            }
            JobDetail jobDetail = jobBuilder.build();
            // 获取 Quartz 触发器实例 trigger；
            CalendarIntervalTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerName, triggerGroup)
                    .withSchedule(CalendarIntervalScheduleBuilder
                            .calendarIntervalSchedule()
                            .withInterval(timeInterval, unit)
                            .withMisfireHandlingInstructionFireAndProceed())
                    .build();
            // 添加监听器
            AIJobListener myJobListener = new AIJobListener(String.format("%s: %s", jobGroup, jobName));
            KeyMatcher<JobKey> keyMatcher = KeyMatcher.keyEquals(jobDetail.getKey());
            scheduler.getListenerManager().addJobListener(myJobListener, keyMatcher);
            // 调度任务；
            scheduler.scheduleJob(jobDetail, trigger);
            LOG.info("创建任务（{}）成功", jobName);
        } catch (SchedulerException se) {
            LOG.error(String.format("创建任务（%s）失败", jobName), se);
        }
    }

    @Override
    public void removeJob(String jobName,
                          String jobGroup,
                          String triggerName,
                          String triggerGroup) {
        try {
            TriggerKey triggerKey = new TriggerKey(triggerName, triggerGroup);
            // 停止触发器；
            scheduler.pauseTrigger(triggerKey);
            // 移除触发器；
            scheduler.unscheduleJob(triggerKey);
            // 删除任务；
            JobKey jobKey = new JobKey(jobName, jobGroup);
            scheduler.deleteJob(jobKey);
            // 移除监听器
            String listenerName = String.format("%s: %s", jobGroup, jobName);
            scheduler.getListenerManager().removeJobListener(listenerName);
            scheduler.getListenerManager().removeJobListenerMatcher(listenerName, KeyMatcher.keyEquals(jobKey));
            String success = scheduler.getTrigger(triggerKey) == null ? "成功" : "失败";
            LOG.info("移除任务（{}）{}", jobName, success);
        } catch (Exception e) {
            LOG.error(String.format("移除任务（%s）失败", jobName), e);
        }
    }

    @Override
    public Set<JobKey> listJobs(GroupMatcher<JobKey> matcher) {
        try {
            return scheduler.getJobKeys(matcher);
        } catch (SchedulerException e) {
            LOG.error("Failed to obtain jobs", e);
            return new HashSet<>();
        }
    }

    @Override
    public Set<TriggerKey> listTriggers(GroupMatcher<TriggerKey> matcher) {
        try {
            return scheduler.getTriggerKeys(matcher);
        } catch (SchedulerException e) {
            LOG.error("Failed to obtain jobs", e);
            return new HashSet<>();
        }
    }

}
