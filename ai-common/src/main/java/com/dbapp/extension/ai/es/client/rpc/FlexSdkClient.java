package com.dbapp.extension.ai.es.client.rpc;

import com.dbapp.extension.ai.es.client.SearchRequestAO;
import com.dbapp.extension.ai.es.config.FlexEsFeignConfiguration;
import com.dbapp.flexsdk.nativees.action.search.SearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "flex-sdk", configuration = FlexEsFeignConfiguration.class)
public interface FlexSdkClient {


    /**
     * 处理普通查询
     */
    @PostMapping("/api/v2/data/search")
    String search(SearchRequestAO searchRequest);

}
