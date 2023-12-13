package com.dbapp.extension.ai.es.impl;

import com.dbapp.extension.ai.es.IEsService;
import com.dbapp.extension.ai.es.client.rpc.FlexSdkClient;
import com.dbapp.extension.ai.es.client.SearchRequestAO;
import com.dbapp.extension.ai.utils.FlexSdkParseUtils;
import jakarta.annotation.Resource;
import com.dbapp.flexsdk.nativees.action.search.SearchRequest;
import com.dbapp.flexsdk.nativees.action.search.SearchResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public final class EsServiceImpl implements IEsService {

    @Resource
    private FlexSdkClient flexSdkClient;

    @Resource
    private FlexSdkParseUtils flexSdkParseUtils;

    @Override
    public SearchResponse search(SearchRequest searchRequest) throws IOException {
        SearchRequestAO searchRequestAO = new SearchRequestAO();
        searchRequestAO.setIndices(searchRequest.indices());
        searchRequestAO.setQueryString(searchRequest.source().toString());
        searchRequestAO.setScrollKeepAliveMillis(searchRequest.scroll().keepAlive().getMillis());
        String response = flexSdkClient.search(searchRequestAO);
        return flexSdkParseUtils.convertJsonToSearchResponse(response);
    }

}
