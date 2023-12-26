package com.dbapp.extension.ai.es.config;


import com.alibaba.fastjson2.util.ParameterizedTypeImpl;
import com.dbapp.boot.core.auth.props.FlexProperties;
import feign.RequestInterceptor;
import feign.Response;
import feign.Retryer;
import feign.codec.Decoder;
import lombok.Data;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.UUID;

public class FlexEsFeignConfiguration {

    public static final String APPLICATION_HEADER = "app";
    public static final String COMPONENT_HEADER = "component";
    public static final String COMPONENT_VALUE = "Es";
    public static final String COMPONENT_VERSION_HEADER = "componentVersion";
    public static final String BASE_VERSION_HEADER = "baseVersion";
    public static final String OP_ID_HEADER = "opId";
    public static final String CACHE_HEADER = "cache";
    public static final String APIKEY_HEADER = "flexApiKey";


    public static String getOpId() {
        return UUID.randomUUID().toString();
    }

    @Bean
    public RequestInterceptor requestInterceptor(FlexSdkConfig flexSdkConfig, FlexProperties flexProperties) {
        return t -> {
            t.header(APPLICATION_HEADER, flexSdkConfig.getApp());
            t.header(BASE_VERSION_HEADER, flexSdkConfig.getBaseVersion());
            t.header(COMPONENT_HEADER, COMPONENT_VALUE);
            t.header(COMPONENT_VERSION_HEADER, flexSdkConfig.getEsConfig().getVersion());
            t.header(OP_ID_HEADER, getOpId());
            t.header(APIKEY_HEADER, flexProperties.getApiKey());
        };
    }

    @Bean
    public Retryer apiKeyRetryer(FlexSdkConfig flexSdkConfig){
        return new Retryer.Default(flexSdkConfig.getPeriod(), flexSdkConfig.getMaxPeriod(), flexSdkConfig.getMaxAttempts());
    }

    @Profile("dev")
    @Bean
    public RequestInterceptor targetInterceptor(FlexSdkConfig flexSdkConfig) {
        return t -> {
            t.target(flexSdkConfig.getUrl());
        };
    }

    @Bean
    public Decoder feignDecoder(ObjectProvider<HttpMessageConverterCustomizer> customizers, ObjectFactory<HttpMessageConverters> messageConverters) {
        return new CustomDecoder(new SpringDecoder(messageConverters, customizers));
    }

    // 响应数据的结构如下：
    // {
    //     "success": true,
    //     "message": "Some message",
    //     "data": { ... }
    // }
    @Data
    public static class SdkResponse<T> {
        private boolean success;
        private String message;
        private String code;
        private T data;

        public <R> SdkResponse clone(R cloneData) {
            SdkResponse sdkResponse = new SdkResponse<R>();
            sdkResponse.setCode(code);
            sdkResponse.setMessage(message);
            sdkResponse.setSuccess(success);
            sdkResponse.setData(cloneData);
            return sdkResponse;
        }
    }

    public static class CustomDecoder implements Decoder {
        private final Decoder delegate;

        public CustomDecoder(Decoder delegate) {
            Objects.requireNonNull(delegate, "Decoder must not be null. ");
            this.delegate = delegate;
        }

        @Override
        public Object decode(Response response, Type type) throws IOException {
            // 调用原始的 Decoder 解析响应数据
            Type actual = new ParameterizedTypeImpl(SdkResponse.class, type);
            Object decodedObject = delegate.decode(response, actual);

            // 对 decodedObject 进行处理，提取 success 和 message 字段的值
            if (decodedObject instanceof SdkResponse sdkResponse) {
                boolean success = sdkResponse.isSuccess();
                String message = sdkResponse.getMessage();

                // 如果成功，则返回 data 字段的数据
                if (success) {
                    return sdkResponse.getData();
                } else {
                    throw new RuntimeException(message);
                }
            }

            // 如果 decodedObject 不是 MyResponse 类型，直接返回
            return decodedObject;
        }
    }
}


