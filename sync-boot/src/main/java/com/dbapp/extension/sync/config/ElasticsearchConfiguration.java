package com.dbapp.extension.sync.config;

import com.dbapp.extension.sync.util.GlobalAttribute;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfiguration {
    @Bean
    public RestHighLevelClient highLevelClient() {
        int socketTimeOut = GlobalAttribute.getPropertyInteger("es.socketTimeOut", 180);
        String[] ips = GlobalAttribute.getPropertyString("es.server.ip", "1.es1,1.es2,1.es3,1.es4").split(",");
        String[] portArr = GlobalAttribute.getPropertyString("es.server.rest.port", "9200").split(",");
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
        return new RestHighLevelClient(restClientBuilder);
    }
}
