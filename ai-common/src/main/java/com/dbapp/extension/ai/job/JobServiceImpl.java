package com.dbapp.extension.ai.job;

import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.client.AdminBizClient;
import com.xxl.job.core.biz.model.*;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author steven.zhu
 * @version 1.0.0
 * @date 2023/12/10
 */
@Service
public class JobServiceImpl implements JobService {

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
