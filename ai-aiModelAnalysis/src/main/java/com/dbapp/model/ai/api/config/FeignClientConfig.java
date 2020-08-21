//package com.dbapp.model.ai.api.config;
//
//import feign.*;
//import feign.codec.Encoder;
//import okhttp3.OkHttpClient;
//import org.springframework.beans.factory.ObjectFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
//import org.springframework.cloud.openfeign.AnnotatedParameterProcessor;
//import org.springframework.cloud.openfeign.support.SpringEncoder;
//import org.springframework.cloud.openfeign.support.SpringMvcContract;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.convert.ConversionService;
//
//import javax.net.ssl.*;
//import java.lang.annotation.Annotation;
//import java.security.SecureRandom;
//import java.security.cert.CertificateException;
//import java.security.cert.X509Certificate;
//import java.util.ArrayList;
//import java.util.List;
//
//@Configuration
//public class FeignClientConfig {
//
//    @Autowired
//    private ObjectFactory<HttpMessageConverters> messageConverters;
//    @Autowired(required = false)
//    private List<AnnotatedParameterProcessor> parameterProcessors = new ArrayList();
//
//    /**
//     * 打印http详情，log4j需开启debug
//     *
//     * @return
//     */
//    @Bean
//    Logger.Level feignLoggerLevel() {
//        return Logger.Level.FULL;
//    }
//
//    @Bean
//    public Client feignClient() {
//        //默认okhttp，开启https协议
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//        builder.sslSocketFactory(createSSLSocketFactory())
//                .hostnameVerifier(new TrustAllHostnameVerifier());
//        return new feign.okhttp.OkHttpClient(builder.build());
//
//    }
//
//    @Bean
//    public Encoder feignEncoder() {
//        return new SpringMultipartEncoder(new SpringEncoder(this.messageConverters));
//    }
//
//    /**
//     * 配置okhttp客户端为https
//     *
//     * @return
//     */
//    private SSLSocketFactory createSSLSocketFactory() {
//        MyTrustManager mMyTrustManager = new MyTrustManager();
//        SSLSocketFactory ssfFactory = null;
//        try {
//            SSLContext sc = SSLContext.getInstance("TLS");
//            sc.init(null, new TrustManager[]{mMyTrustManager}, new SecureRandom());
//            ssfFactory = sc.getSocketFactory();
//        } catch (Exception ignored) {
//            ignored.printStackTrace();
//        }
//
//        return ssfFactory;
//    }
//
//    /**
//     * 证书库为空，不做证书校验
//     */
//    public class MyTrustManager implements X509TrustManager {
//        @Override
//        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//        }
//
//        @Override
//        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//        }
//
//        @Override
//        public X509Certificate[] getAcceptedIssuers() {
//            return new X509Certificate[0];
//        }
//    }
//
//    /**
//     * 信任所有https服务端
//     */
//    private class TrustAllHostnameVerifier implements HostnameVerifier {
//        @Override
//        public boolean verify(String hostname, SSLSession session) {
//            return true;
//        }
//    }
//
//    /**
//     * 添加NotHttp注解判断
//     * 如果有NotHttp注解，不参与到http序列化过程中，如果接口服务异常，直接透传到fallback
//     *
//     * @param feignConversionService
//     * @return
//     */
//    @Bean
//    public Contract feignContract(ConversionService feignConversionService) {
//        return new SpringMvcContract(this.parameterProcessors, feignConversionService) {
//            @Override
//            protected boolean processAnnotationsOnParameter(MethodMetadata data, Annotation[] annotations, int paramIndex) {
//                //如果有NotHttp注解，不参与http序列化操作
//                for (Annotation annotation : annotations) {
//                    if (annotation instanceof NotHttp) {
//                        return true;
//                    }
//                }
//                return super.processAnnotationsOnParameter(data, annotations, paramIndex);
//            }
//        };
//    }
//
//    /**
//     * 取消失败重试机制
//     *
//     * @return
//     */
//    @Bean
//    public Retryer retryer() {
//        return Retryer.NEVER_RETRY;
//    }
//}
