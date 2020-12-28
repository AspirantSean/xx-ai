package com.dbapp.extension.ai.quartz;

import org.quartz.DateBuilder;
import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.Map;
import java.util.Set;

public interface IQuartzManager {

    void addJob(String jobName,
                String jobGroup,
                String triggerName,
                String triggerGroup,
                Class<? extends Job> jobClass,
                int timeInterval,
                DateBuilder.IntervalUnit unit);

    /**
     * 添加一个定时任务，使用传入的任务名、任务群组名、触发器名、触发器群组名；
     *
     * @param jobName      任务名；
     * @param jobGroup     任务的群组名；
     * @param triggerName  触发器名；
     * @param triggerGroup 触发器群组名；
     * @param jobClass     任务类；
     * @param timeInterval 用于指定任务的执行时间间隔；
     * @param unit         间隔单位(MILLISECOND,SECOND,MINUTE,HOUR,DAY,WEEK,MONTH,YEAR)；
     * @param data         任务数据
     * @return 返回任务添加成功与否的数据
     */
    void addJob(String jobName,
                String jobGroup,
                String triggerName,
                String triggerGroup,
                Class<? extends Job> jobClass,
                int timeInterval,
                DateBuilder.IntervalUnit unit,
                Map<String, Object> data);

    /**
     * 移除指定任务；
     *
     * @param jobName      任务名；
     * @param jobGroup     任务的群组名；
     * @param triggerName  触发器名；
     * @param triggerGroup 触发器群组名；
     * @return 返回任务移除成功与否的数据
     */
    void removeJob(String jobName,
                   String jobGroup,
                   String triggerName,
                   String triggerGroup);

    Set<JobKey> listJobs(GroupMatcher<JobKey> matcher);

    Set<TriggerKey> listTriggers(GroupMatcher<TriggerKey> matcher);
}
