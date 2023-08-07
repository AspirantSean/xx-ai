package com.dbapp.extension.sync.prototype.es;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Cancellable;
import org.elasticsearch.client.indices.IndexTemplatesExistRequest;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;

import java.io.IOException;

public interface IEsService {

    SearchResponse search(SearchRequest searchRequest) throws IOException;

    boolean existTemplate(IndexTemplatesExistRequest indexTemplatesExistRequest) throws IOException;

    AcknowledgedResponse putTemplate(PutIndexTemplateRequest putIndexTemplateRequest) throws IOException;

    BulkResponse bulk(BulkRequest bulkRequest) throws IOException;

    Cancellable bulkAsync(BulkRequest bulkRequest, ActionListener<BulkResponse> listener);

    RefreshResponse refreshIndex(RefreshRequest refreshRequest) throws IOException;
}
