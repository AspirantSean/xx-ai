package com.dbapp.extension.ai.config.source;

import feign.Client;
import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @ClassName FeignConfig
 * @Description 默认okhttp，支持https
 * @Author joker
 * @Date 2019-07-18 21:11
 * @Version 1.0
 **/
@Slf4j
@Configuration
public class FeignClientConfig {

    /**
     * 打印http详情，log4j需开启debug
     *
     * @return
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * 默认带上Cookie、Referer，多线程情况下会失效，需要单独处理
     * Cookie，用户会话，app可直接获取用户信息
     * Referer，通过trustReferer校验
     *
     * @return
     */
    @Bean
    public RequestInterceptor interceptor() {
        return t -> {
            try {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                if (request != null) {
                    String cookie = request.getHeader("Cookie");
                    t.header("Cookie", cookie);
                    if (!t.headers().containsKey("Referer")) {
                        t.header("Referer", request.getHeader("Referer"));
                    }
                }
            } catch (IllegalStateException e) {
                log.debug("rpc接口，获取当前request-cookie失败");
            }
        };
    }

    /**
     * 取消失败重试机制
     *
     * @return
     */
    @Bean
    public Retryer retryer() {
        return Retryer.NEVER_RETRY;
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
        SSLSocketFactory sslFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{mMyTrustManager}, new SecureRandom());
            sslFactory = sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sslFactory;
    }


    /**
     * 证书库为空，不做证书校验
     */
    public static class MyTrustManager implements X509TrustManager {
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
