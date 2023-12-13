package com.dbapp.extension.ai.es;

import com.dbapp.flexsdk.nativees.action.search.SearchRequest;
import com.dbapp.flexsdk.nativees.action.search.SearchResponse;

import java.io.IOException;

public interface IEsService {

    SearchResponse search(SearchRequest searchRequest) throws IOException;

}
