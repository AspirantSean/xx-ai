package com.dbapp.extension.ai.es.impl;

import com.dbapp.extension.ai.es.IEsService;
import com.dbapp.extension.ai.utils.GlobalAttribute;
import com.dbapp.nacos.config.GlobalConfig;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Service
public final class EsServiceImpl implements IEsService {

    @Autowired
    private GlobalConfig globalConfig;

    private RestHighLevelClient highLevelClient;
    private int socketTimeOut;

    @PostConstruct
    private void init() {
        socketTimeOut = GlobalAttribute.getPropertyInteger("es.socketTimeOut", 180);
        String[] ips = globalConfig.getString("es_server_ip", "1.es1,1.es2").split(",");
        String[] portArr = globalConfig.getString("es_server_rest_port", "9200").split(",");
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
