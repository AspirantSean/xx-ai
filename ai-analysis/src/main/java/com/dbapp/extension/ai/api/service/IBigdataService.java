package com.dbapp.extension.ai.api.service;

import com.dbapp.extension.mirror.dto.AIModel;
import com.dbapp.extension.mirror.dto.MetricInfo;

import java.util.List;

public interface IBigdataService {
    List<AIModel> getAIModelList();

    MetricInfo getMetricInfo(String metricId, long startTime, long endTime);
}
