package com.dbapp.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public final class MetricsUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MetricsUtils.class);

    /**
     * 超时设置
     */
    private static final int TIME_OUT = 3600000;
    /**
     * 请求配置
     */
    private static RequestConfig requestConfig =
            RequestConfig.custom()
                    .setConnectTimeout(TIME_OUT)
                    .setConnectionRequestTimeout(TIME_OUT)
                    .setSocketTimeout(TIME_OUT)
                    .build();

    private static SSLConnectionSocketFactory sslSF = null;
    private static SSLContextBuilder builder = new SSLContextBuilder();

    static {
        try {
            builder.loadTrustMaterial(null, (TrustStrategy) (x509Certificates, s) -> true);
            sslSF = new SSLConnectionSocketFactory(builder.build(), new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static String mirrorAddress = GlobalAttribute.getPropertyString("mirrorAddress", "http://127.0.0.1:8901");

    /**
     * 获取http client
     *
     * @return
     */
    private static CloseableHttpClient getHttpClient() {
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(TIME_OUT)
                .setConnectTimeout(TIME_OUT)
                .setConnectionRequestTimeout(TIME_OUT)
                .build();
        CloseableHttpClient httpClient;
        httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslSF)
                .setDefaultRequestConfig(defaultRequestConfig)
                .setConnectionManagerShared(false)
                .build();
        return httpClient;
    }

    public Map<String, Object> getMetricInfo(String metricId) throws IOException {
        String url = String.format("%s/api/metric/getMetricInfo?metricId=%s", mirrorAddress, metricId);
        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(requestConfig);
            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Accept", "application/json");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity resEntity = httpResponse.getEntity();
            JSONObject response = resEntity != null ? JSONObject.parseObject(EntityUtils.toString(resEntity)) : new JSONObject();
            if (statusCode == HttpStatus.SC_OK
                    && response.getInteger("code") == 0) {
                JSONObject metricInfo = response.getJSONObject("data");
                return metricInfo.getInnerMap();
            } else {
                throw new RuntimeException("获取指标信息失败");
            }
        }
    }
}
