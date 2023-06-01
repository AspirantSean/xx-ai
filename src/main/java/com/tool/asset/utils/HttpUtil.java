package com.tool.asset.utils;

import cn.hutool.core.net.URLEncodeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public final class HttpUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取http client
     *
     * @return
     */
    public static CloseableHttpClient getHttpClient() {
        // 超时设置
        int TIME_OUT = 3600000;
        // 组装httpclient代理
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        // 组装httpclient配置
        return httpClientBuilder
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setSocketTimeout(TIME_OUT)
                        .setConnectTimeout(TIME_OUT)
                        .setConnectionRequestTimeout(TIME_OUT)
                        .build())
                .setConnectionManagerShared(false)
                .setSSLContext(((Supplier<SSLContext>) () -> {
                    SSLContext sslContext = null;
                    try {
                        sslContext = SSLContext.getInstance("TLS");
                        sslContext.init(null, new TrustManager[]{
                                new X509TrustManager() {
                                    @Override
                                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                                        // Do nothing.
                                    }

                                    @Override
                                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                                        // Do nothing.
                                    }

                                    @Override
                                    public X509Certificate[] getAcceptedIssuers() {
                                        return new X509Certificate[0];
                                    }
                                }
                        }, new SecureRandom());
                    } catch (NoSuchAlgorithmException | KeyManagementException ignored) {
                        log.error("SSL init Exception", ignored);
                    }
                    return sslContext;
                }).get())
                .setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
    }

    /**
     * 发送GET或POST请求，http、https兼容，不抛出异常
     *
     * @param url
     * @param method
     * @param headers
     * @param params
     * @param body
     * @return
     * @throws IOException
     */
    public static JSONObject requestForJSONObjectIgnoreException(String url,
                                                                 String method,
                                                                 Map<String, String> headers,
                                                                 Map<String, Object> params,
                                                                 Map<String, Object> body) {
        try {
            return requestForJSONObject(url, method, headers, params, body);
        } catch (IOException e) {
            log.error("请求" + url + "失败", e);
            return null;
        }
    }

    /**
     * 发送GET或POST请求，http、https兼容
     *
     * @param url
     * @param method
     * @param headers
     * @param params
     * @param body
     * @return
     * @throws IOException
     */
    public static JSONObject requestForJSONObject(String url,
                                                  String method,
                                                  Map<String, String> headers,
                                                  Map<String, Object> params,
                                                  Map<String, Object> body) throws IOException {
        if ("POST".equalsIgnoreCase(method)) {
            return post(url, headers, params, body, JSONObject.class);
        } else if ("GET".equalsIgnoreCase(method)) {
            return get(url, headers, params, JSONObject.class);
        } else {
            // 暂不支持其他请求方式
            return null;
        }
    }

    /**
     * 发送post请求，http、https兼容
     *
     * @param url
     * @param headers
     * @param params
     * @param body
     * @param resultClazz
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T post(String url,
                             Map<String, String> headers,
                             Map<String, Object> params,
                             Map<String, Object> body,
                             Class<T> resultClazz) throws IOException {
        // 获取请求体
        HttpPost httpPost = buildRequest(url, headers, params, HttpPost::new);
        // 组装请求体
        httpPost.setEntity(new StringEntity(JSON.toJSONString(body), ContentType.APPLICATION_JSON));
        // 请求
        HttpResponse httpResponse = getHttpClient().execute(httpPost);
        // 响应处理
        return dealResponse(httpResponse, resultClazz);
    }

    /**
     * 发送get请求，http、https兼容
     *
     * @param url
     * @param headers
     * @param params
     * @param resultClazz
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T get(String url,
                            Map<String, String> headers,
                            Map<String, Object> params,
                            Class<T> resultClazz) throws IOException {
        // 请求
        HttpRequestBase httpGet = buildRequest(url, headers, params, HttpGet::new);
        HttpResponse httpResponse = getHttpClient().execute(httpGet);
        log.info(httpGet.toString());
        // 响应处理
        return dealResponse(httpResponse, resultClazz);
    }

    /**
     * 组装请求，GET、POST
     *
     * @param url
     * @param headers
     * @param params
     * @param requestFunction
     * @param <R>
     * @return
     */
    private static <R extends HttpRequestBase> R buildRequest(String url,
                                                              Map<String, String> headers,
                                                              Map<String, Object> params,
                                                              Function<URI, R> requestFunction) {
        // 组装url及参数
        URI uri;
        if (params != null && params.size() > 0) {
            uri = URI.create(params.entrySet()
                    .stream()
                    .map(entry -> URLEncodeUtil.encode(entry.getKey() + "=" + entry.getValue()))
                    .collect(Collectors.joining("&", url + "?", "")));
        } else {
            uri = URI.create(url);
        }
        R request = requestFunction.apply(uri);
        // 组装请求头
        if (headers != null && headers.size() > 0) {
            request.setHeaders(headers.entrySet()
                    .stream()
                    .map(header -> new BasicHeader(header.getKey(), header.getValue()))
                    .toArray(BasicHeader[]::new));
        }
        return request;
    }

    /**
     * 响应处理
     *
     * @param httpResponse
     * @param resultClazz
     * @param <T>
     * @return
     * @throws IOException
     */
    private static <T> T dealResponse(HttpResponse httpResponse, Class<T> resultClazz) throws IOException {
        int statusCode = httpResponse.getStatusLine().getStatusCode();// 响应码
        if (HttpStatus.SC_OK == statusCode) {// 请求成功
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                return objectMapper.readValue(EntityUtils.toString(httpEntity), resultClazz);
            } else {
                return null;
            }
        } else {// 失败
            throw new RuntimeException(httpResponse.toString());
        }
    }

}
