
package com.dbapp.extension.ai.baas.rpc;

import com.dbapp.extension.ai.baas.dto.entity.ModelMetric;
import com.dbapp.extension.ai.baas.dto.entity.model.ai.ModelAI;
import com.dbapp.extension.mirror.dto.AIModel;
import java.util.List;

import com.dbapp.extension.mirror.response.BigdataResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@FeignClient(
    contextId = "baasClientAi",
    name = "baas"
)
public interface                                                                                                                                                                                                                                                                                IBaasClient {
    @RequestMapping(
        value = {"/baas/api/v1/strategy/ai/enable"},
        method = {RequestMethod.GET}
    )
    BigdataResponse<List<AIModel>> getAIModelList();

    /**
     * 根据英文名列表查询特定模型或指标列表
     *
     * @param ruleIds
     * @return
     */
    @RequestMapping(value = "/baas/api/v1/strategy/mirror/byruleid/batch", method = GET)
    BigdataResponse<List<ModelMetric>> getModelMetricByRuleIds(@RequestParam("ruleId") List<String> ruleIds);

    @RequestMapping(value = "/baas/api/v1/strategy/mirror/byruleid/formirror", method = GET)
    <T extends ModelMetric> BigdataResponse<T> getModelMetric(@RequestParam("ruleId") String ruleId);

    @RequestMapping(value = "/baas/api/v1/strategy/mirror/ai/enable", method = GET)
    <T extends ModelMetric> BigdataResponse<List<T>> getAllEnableAIModels();

}
