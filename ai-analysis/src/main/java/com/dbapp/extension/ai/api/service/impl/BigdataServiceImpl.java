package com.dbapp.extension.ai.api.service.impl;


import com.dbapp.extension.ai.api.invoker.BigdataInvoker;
import com.dbapp.extension.ai.api.service.IBigdataService;
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

}
