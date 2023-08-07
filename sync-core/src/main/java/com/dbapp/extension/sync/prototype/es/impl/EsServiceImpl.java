package com.dbapp.extension.sync.prototype.es.impl;

import com.dbapp.extension.sync.prototype.es.IEsService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.IndexTemplatesExistRequest;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Service
public final class EsServiceImpl implements IEsService {

    @Resource
    private RestHighLevelClient highLevelClient;

    @Override
    public SearchResponse search(SearchRequest searchRequest) throws IOException {
        log.info("elasticsearch: search request is " + searchRequest.toString());
        return highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    }

    @Override
    public boolean existTemplate(IndexTemplatesExistRequest indexTemplatesExistRequest) throws IOException {
        log.info("elasticsearch: get template name is " + indexTemplatesExistRequest.names());
        return highLevelClient.indices().existsTemplate(indexTemplatesExistRequest, RequestOptions.DEFAULT);
    }

    @Override
    public AcknowledgedResponse putTemplate(PutIndexTemplateRequest putIndexTemplateRequest) throws IOException {
        if (existTemplate(new IndexTemplatesExistRequest(putIndexTemplateRequest.name()))) {
            log.info("elasticsearch: template which called {} is existed", putIndexTemplateRequest.name());
            return AcknowledgedResponse.TRUE;
        }
        log.info("elasticsearch: put template request is " + putIndexTemplateRequest);
        return highLevelClient.indices().putTemplate(putIndexTemplateRequest, RequestOptions.DEFAULT);
    }

    @Override
    public BulkResponse bulk(BulkRequest bulkRequest) throws IOException {
        log.info("elasticsearch: bulk request is " + bulkRequest.requests());
        return highLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    @Override
    public Cancellable bulkAsync(BulkRequest bulkRequest, ActionListener<BulkResponse> listener) {
        log.info("elasticsearch: bulk request is " + bulkRequest.requests());
        return highLevelClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, listener);
    }

    @Override
    public RefreshResponse refreshIndex(RefreshRequest refreshRequest) throws IOException {
        return highLevelClient.indices().refresh(refreshRequest, RequestOptions.DEFAULT);
    }

}
