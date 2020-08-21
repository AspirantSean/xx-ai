package com.dbapp.model.ai.api.service.impl;

import com.dbapp.app.mirror.dto.AIModel;
import com.dbapp.app.mirror.dto.MetricInfo;
import com.dbapp.app.mirror.rpc.IBaasClient;
import com.dbapp.app.mirror.rpc.IMetricClient;
import com.dbapp.model.ai.api.invoker.BigdataInvoker;
import com.dbapp.model.ai.api.service.IBigdataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
