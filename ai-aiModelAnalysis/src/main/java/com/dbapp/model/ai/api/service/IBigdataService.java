package com.dbapp.model.ai.api.service;

import com.dbapp.model.ai.entity.AIModel;
import com.dbapp.model.ai.entity.MetricInfo;

import java.util.List;

public interface IBigdataService {
    List<AIModel> getAIModelList();

    MetricInfo getMetricInfo(String metricId, long startTime, long endTime);
}
