package com.dbapp.extension.ai.job;

import com.xxl.job.core.biz.model.JobInfoResult;
import com.xxl.job.core.biz.model.RegistryJobParam;

import java.util.List;
import java.util.Map;

/**
 * @author steven.zhu
 * @version 1.0.0
 * @date 2023/12/9
 */
public interface JobService {


    void deleteByHandler(String handler);

    void deleteByOtherKey(String otherKey);

    void addJob(RegistryJobParam jobInfo);

    List<JobInfoResult> getJobsByHandler(String handler);

    JobInfoResult getJobByOtherKey(String otherKey);

    boolean trigger(int id);
}
