package com.dbapp.es;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;

import java.io.IOException;

public interface IEsService {

    SearchResponse search(SearchRequest searchRequest) throws IOException;

}
