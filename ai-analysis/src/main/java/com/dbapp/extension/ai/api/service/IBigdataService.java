package com.dbapp.extension.ai.api.service;

import com.dbapp.extension.ai.baas.dto.entity.ModelMetric;
import com.dbapp.extension.ai.baas.dto.entity.model.ai.ModelAI;
import com.dbapp.extension.mirror.dto.AIModel;
import com.dbapp.extension.mirror.dto.MetricInfo;

import java.util.List;

public interface IBigdataService {
    List<AIModel> getAIModelList();

    MetricInfo getMetricInfo(String metricId, long startTime, long endTime);

    List<ModelMetric> getModelMetricByRuleIds(List<String> modelIds);

    <T extends ModelMetric> T getModelMetric(String modelId);

    List<ModelAI> getAllEnableAIModels();

}
