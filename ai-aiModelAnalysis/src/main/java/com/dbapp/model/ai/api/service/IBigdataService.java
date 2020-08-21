package com.dbapp.model.ai.api.service;

import com.dbapp.app.mirror.dto.AIModel;
import com.dbapp.app.mirror.dto.MetricInfo;

import java.util.List;

public interface IBigdataService {
    List<AIModel> getAIModelList();

    MetricInfo getMetricInfo(String metricId, long startTime, long endTime);
}
