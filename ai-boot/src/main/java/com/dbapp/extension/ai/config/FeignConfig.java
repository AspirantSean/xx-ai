package com.dbapp.extension.ai.config;

import feign.Client;
import feign.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @ClassName FeignConfig
 * @Description TODO
 * @Author joker.tong
 * @Date 2020/11/25 13:59
 * @Version 1.0
 **/
@Configuration
public class FeignConfig {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(FeignConfig.class);
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Client feignClient(LoadBalancerClient loadBalancerClient, LoadBalancerClientFactory loadBalancerClientFactory) {
        return new FeignBlockingLoadBalancerClient(new Client.Default(createSSLSocketFactory(), (s, session) -> true), loadBalancerClient, loadBalancerClientFactory);
    }

    /**
     * 配置okhttp客户端为https
     *
     * @return
     */
    private SSLSocketFactory createSSLSocketFactory() {
        MyTrustManager mMyTrustManager = new MyTrustManager();
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{mMyTrustManager}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return ssfFactory;
    }

    /**
     * 证书库为空，不做证书校验
     */
    public class MyTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
