package com.dbapp.model.ai.api;

import com.dbapp.model.ai.api.config.BigdataConfig;
import com.dbapp.model.ai.api.response.BigdataResponse;
import com.dbapp.model.ai.entity.AIModel;
import com.dbapp.model.ai.entity.MetricInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "BigdataServiceApi", url = "${mirrorAddress}", configuration = BigdataConfig.class)
public interface BigdataServiceApi {

    /**
     * 获取所有AI模型
     *
     * @return AI模型
     */
    @RequestMapping(value = "/baas/api/v1/strategy/ai/enable", method = RequestMethod.GET)
    BigdataResponse<List<AIModel>> getAIModelList();

    /**
     * 获取指标信息
     *
     * @return 指标信息
     */
    @RequestMapping(value = "/api/metric/getMetricQueryInfo", method = RequestMethod.GET)
    BigdataResponse<MetricInfo> getMetricInfo(@RequestParam("metricId") String metricId,
                                              @RequestParam("startTime") long startTime,
                                              @RequestParam("endTime") long endTime);

}
