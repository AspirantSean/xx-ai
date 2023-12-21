package com.dbapp.extension.ai.api.service.impl;


import com.dbapp.extension.ai.api.invoker.BigdataInvoker;
import com.dbapp.extension.ai.api.service.IBigdataService;
import com.dbapp.extension.ai.baas.dto.entity.ModelMetric;
import com.dbapp.extension.ai.baas.dto.entity.model.ai.ModelAI;
import com.dbapp.extension.ai.baas.rpc.IBaasClient;
import com.dbapp.extension.mirror.dto.AIModel;
import com.dbapp.extension.mirror.dto.MetricInfo;
import com.dbapp.extension.mirror.rpc.IMetricClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BigdataServiceImpl implements IBigdataService {


    @Resource
    private IBaasClient iBaasClient;

    @Resource
    private IMetricClient iMetricClient;

    @Override
    public List<AIModel> getAIModelList() {
        List<AIModel> data = BigdataInvoker.invoke(() -> iBaasClient.getAIModelList());
        if (data == null) {
            return new ArrayList<>();
        } else {
            return data;
        }
    }

    @Override
    public MetricInfo getMetricInfo(String metricId, long startTime, long endTime) {
        return BigdataInvoker.invoke(() -> iMetricClient.getMetricInfo(metricId, startTime, endTime));
    }

    @Override
    public List<ModelMetric> getModelMetricByRuleIds(List<String> modelIds) {
        return BigdataInvoker.invoke(() -> iBaasClient.getModelMetricByRuleIds(modelIds));
    }

    @Override
    public <T extends ModelMetric> T getModelMetric(String modelId) {
        return BigdataInvoker.invoke(() -> iBaasClient.getModelMetric(modelId));
    }

    @Override
    public List<ModelAI> getAllEnableAIModels() {
        return BigdataInvoker.invoke(() -> iBaasClient.getAllEnableAIModels());
    }

}
