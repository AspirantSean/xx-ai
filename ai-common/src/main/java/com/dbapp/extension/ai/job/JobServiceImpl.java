package com.dbapp.extension.ai.job;

import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.client.AdminBizClient;
import com.xxl.job.core.biz.model.*;
import com.xxl.job.core.config.ExecutorProperties;
import com.xxl.job.core.enums.ExecutorRouteStrategyEnum;
import com.xxl.job.core.enums.MisfireStrategyEnum;
import com.xxl.job.core.enums.ScheduleTypeEnum;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.glue.GlueTypeEnum;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xxl.job.core.enums.ExecutorBlockStrategyEnum.DISCARD_LATER;

/**
 * @author steven.zhu
 * @version 1.0.0
 * @date 2023/12/10
 */
@Service
public class JobServiceImpl implements JobService {

    @Resource
    private ExecutorProperties executorProperties;

    private AtomicInteger counter = new AtomicInteger();

    private AdminBiz nextClient() {
        int part = toPositive(counter.getAndIncrement()) % XxlJobExecutor.getAdminBizList().size();
        return XxlJobExecutor.getAdminBizList().get(part);
    }

    private int toPositive(int number) {
        return number & 0x7fffffff;
    }

    @Override
    public void deleteByHandler(String handler) {
        AdminBiz adminBiz = nextClient();
        ReturnT<Integer> integerReturnT = adminBiz.deleteJobsByHandler(handler);
        if (integerReturnT.getCode() != 200) {
            throw new RuntimeException(integerReturnT.getMsg());
        }
    }


    @Override
    public void deleteByOtherKey(String otherKey) {
        AdminBiz adminBiz = nextClient();
        ReturnT<String> stringReturnT = adminBiz.removeJobByOtherKey(otherKey);
        if (stringReturnT.getCode() != 200) {
            throw new RuntimeException(stringReturnT.getMsg());
        }
    }

    @Override
    public void addJob(RegistryJobParam jobInfo) {
        jobInfo.setAppName(executorProperties.getAppname());
        if (StringUtils.isBlank(jobInfo.getExecutorRouteStrategy())) {
            jobInfo.setExecutorRouteStrategy(ExecutorRouteStrategyEnum.ROUND.name());
        }

        if (StringUtils.isBlank(jobInfo.getExecutorBlockStrategy())) {
            jobInfo.setExecutorBlockStrategy(DISCARD_LATER.name());
        }

        if (StringUtils.isBlank(jobInfo.getMisfireStrategy())) {
            jobInfo.setMisfireStrategy(MisfireStrategyEnum.DO_NOTHING.name());
        }

        if (StringUtils.isBlank(jobInfo.getScheduleType())) {
            jobInfo.setScheduleType(ScheduleTypeEnum.CRON.name());
        }

        if (StringUtils.isBlank(jobInfo.getGlueType())) {
            jobInfo.setGlueType(GlueTypeEnum.BEAN.name());
        }

        if (StringUtils.isBlank(jobInfo.getRegisterWay())) {
            jobInfo.setRegisterWay("restful");
        }
        AdminBiz adminBiz = nextClient();
        ReturnT<String> stringReturnT = adminBiz.addJob(jobInfo);
        if (stringReturnT.getCode() != 200) {
            throw new RuntimeException(stringReturnT.getMsg());
        }
    }

    @Override
    public List<JobInfoResult> getJobsByHandler(String handler) {
        AdminBiz adminBiz = nextClient();
        ReturnT<List<JobInfoResult>> jobsByHandler = adminBiz.getJobsByHandler(handler);
        if (jobsByHandler.getCode() != 200) {
            throw new RuntimeException(jobsByHandler.getMsg());
        }
        return jobsByHandler.getContent();
    }

    @Override
    public JobInfoResult getJobByOtherKey(String otherKey) {
        AdminBiz adminBiz = nextClient();

        QueryJobListOption queryJobListOption = new QueryJobListOption();
        List<String> otherKeys = new ArrayList<>();
        otherKeys.add(otherKey);
        queryJobListOption.setOtherKeys(otherKeys);
        queryJobListOption.setPage(new Page(1,1));

        ReturnT<List<JobInfoResult>> listReturnT = adminBiz.queryJobs(queryJobListOption);
        if (listReturnT.getCode() != 200) {
            throw new RuntimeException(listReturnT.getMsg());
        }
        List<JobInfoResult> content = listReturnT.getContent();
        return content != null && content.size() != 0 ? content.get(0) : null;
    }

    @Override
    public boolean trigger(int id) {
        AdminBiz adminBiz = nextClient();
        ReturnT<String> stringReturnT = adminBiz.triggerJob(id, null, null);
        if (stringReturnT.getCode() != 200) {
            throw new RuntimeException(stringReturnT.getMsg());
        }
        return true;
    }
}
