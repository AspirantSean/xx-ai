package com.dbapp.model.ai.api.service.impl;

import com.dbapp.model.ai.api.BigdataServiceApi;
import com.dbapp.model.ai.api.invoker.BigdataInvoker;
import com.dbapp.model.ai.api.service.IBigdataService;
import com.dbapp.model.ai.entity.AIModel;
import com.dbapp.model.ai.entity.MetricInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class BigdataServiceImpl implements IBigdataService {

    @Resource
    private BigdataServiceApi bigdataServiceApi;

    @Override
    public List<AIModel> getAIModelList() {
        List<AIModel> data = BigdataInvoker.invoke(() -> bigdataServiceApi.getAIModelList());
        if (data == null) {
            return new ArrayList<>();
        } else {
            return data;
        }
    }

    @Override
    public MetricInfo getMetricInfo(String metricId, long startTime, long endTime) {
        return BigdataInvoker.invoke(() -> bigdataServiceApi.getMetricInfo(metricId, startTime, endTime));
    }

}
