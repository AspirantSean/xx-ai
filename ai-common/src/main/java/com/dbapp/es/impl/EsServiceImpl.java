package com.dbapp.es.impl;

import com.dbapp.es.IEsService;
import com.dbapp.utils.GlobalAttribute;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public final class EsServiceImpl implements IEsService {

    private RestHighLevelClient highLevelClient;
    private int socketTimeOut;

    private EsServiceImpl() {
        socketTimeOut = GlobalAttribute.getPropertyInteger("es.socketTimeOut", 180);
        String[] ips = GlobalAttribute.getPropertyString("es.server.ip", "192.168.31.39").split(",");
        String[] portArr = GlobalAttribute.getPropertyString("es.server.restPort", "19201").split(",");
        HttpHost[] hosts = new HttpHost[ips.length];
        for (int i = 0; i < ips.length; i++) {
            if (i < portArr.length) {
                hosts[i] = new HttpHost(ips[i], Integer.parseInt(portArr[i]), "http");
            } else {
                hosts[i] = new HttpHost(ips[i], Integer.parseInt(portArr[0]), "http");
            }
        }
        RestClientBuilder restClientBuilder = RestClient.builder(hosts).
                setRequestConfigCallback(requestConfigBuilder -> {
                    requestConfigBuilder.setConnectTimeout(5000);
                    requestConfigBuilder.setSocketTimeout(socketTimeOut * 1000);
                    requestConfigBuilder.setConnectionRequestTimeout(1000);
                    return requestConfigBuilder;
                });
        highLevelClient = new RestHighLevelClient(restClientBuilder);
    }

    @Override
    public SearchResponse search(SearchRequest searchRequest) throws IOException {
        return highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    }

}
