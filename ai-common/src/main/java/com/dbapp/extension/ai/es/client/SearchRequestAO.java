package com.dbapp.extension.ai.es.client;

import lombok.Data;

@Data
public class SearchRequestAO {

    private String[] indices;

    private String queryString;

    private Long scrollKeepAliveMillis;

    private Integer bufferSize;
}

