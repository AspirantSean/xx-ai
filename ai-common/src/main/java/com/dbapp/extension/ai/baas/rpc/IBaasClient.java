
package com.dbapp.extension.ai.baas.rpc;

import com.dbapp.extension.mirror.dto.AIModel;
import java.util.List;

import com.dbapp.extension.mirror.response.BigdataResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(
    contextId = "baasClientAi",
    name = "baas"
)
public interface IBaasClient {
    @RequestMapping(
        value = {"/baas/api/v1/strategy/ai/enable"},
        method = {RequestMethod.GET}
    )
    BigdataResponse<List<AIModel>> getAIModelList();

}
