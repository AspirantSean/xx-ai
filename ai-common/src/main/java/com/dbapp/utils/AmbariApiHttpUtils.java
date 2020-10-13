package com.dbapp.utils;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: AmbariApiHttpUtils
 * @Description: Ambari api接口的http请求工具类
 * @author: limu
 * @date: 2017年7月7日 下午2:13:38
 * PS:考虑到ambari post请求会给集群环境带来变化，暂时未写post的请求封装
 */
public final class AmbariApiHttpUtils {
    private static Log log = LogFactory.getLog(AmbariApiHttpUtils.class);
    private static String ambari_passwd = GlobalAttribute.getPropertyString("ambari_passwd", "Ca9BNyxj28");

    private static final String CHARSET = "UTF-8";

    /**
     * 执行一个HTTP GET请求，返回请求响应的HTML
     *
     * @param url 请求的URL地址
     *            //   * @param queryString 请求的查询参数,可以为null
     *            //   * @param charset 字符集
     *            //   * @param pretty 是否美化
     * @return 返回请求响应的HTML
     */
    public static String doGet(String url) {
        String queryString = null;
        StringBuffer response = new StringBuffer();
        HttpClient httpClient = getHttpClient();
        HttpMethod method = new GetMethod(url);
        try {
            httpClient.executeMethod(method);
            if (method.getStatusCode() == HttpStatus.SC_OK) {
                concatResult(response, method);
            }
        } catch (URIException e) {
            log.error("执行HTTP Get请求时，编码查询字符串“" + queryString + "”发生异常！", e);
        } catch (IOException e) {
            log.error("执行HTTP Get请求" + url + "时，发生异常！", e);
        } finally {
            method.releaseConnection();
        }
        return response.toString();
    }

    public static String doPut(String url, String body) {
        StringBuffer response = new StringBuffer();
        HttpClient httpClient = getHttpClient();
        PutMethod method = new PutMethod(url);
        method.setRequestBody(body);
        try {
            int statusCode = 0;
            if (StringUtils.isNotBlank(body))
            // 对get请求参数做了http请求默认编码，好像没有任何问题，汉字编码后，就成为%式样的字符串
            {
                statusCode = httpClient.executeMethod(method);
            }
            if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_ACCEPTED) {
                concatResult(response, method);
                return response.toString();
            } else {
                concatResult(response, method);
                log.error("执行HTTP PUT请求" + url + "时，发生异常！ msg: " + response.toString());
            }
        } catch (IOException e) {
            log.error("执行HTTP PUT请求" + url + "时，发生异常！", e);
        } finally {
            method.releaseConnection();
        }
        return "failed";
    }

    public static String doPutUTF8(String url, String body) {
        StringBuffer response = new StringBuffer();
        HttpClient httpClient = getHttpClient();
        httpClient.getParams().setParameter(
                HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
        PutMethod method = new PutMethod(url);
        method.setRequestBody(body);
        try {
            int statusCode = 0;
            if (StringUtils.isNotBlank(body))
            // 对get请求参数做了http请求默认编码，好像没有任何问题，汉字编码后，就成为%式样的字符串
            {
                statusCode = httpClient.executeMethod(method);
            }
            if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_ACCEPTED) {
                concatResult(response, method);
                return response.toString();
            } else {
                concatResult(response, method);
                log.error("执行HTTP PUT请求" + url + "时，发生异常！ msg: " + response.toString());
            }
        } catch (IOException e) {
            log.error("执行HTTP PUT请求" + url + "时，发生异常！", e);
        } finally {
            method.releaseConnection();
        }
        return "failed";
    }


    public static HttpClient getHttpClient() {
        HttpClient httpClient =
                new HttpClient(new HttpClientParams(), new SimpleHttpConnectionManager(true));
        List<Header> headers;
        headers = new ArrayList<Header>();
        //读取ambari 访问密码
        String encoding = new String(Base64.encodeBase64(("admin:" + ambari_passwd + "").getBytes()));
        headers.add(new Header("Authorization", "Basic " + encoding));
        headers.add(new Header("X-Requested-By", "ambari"));
        httpClient.getHostConfiguration().getParams().setParameter("http.default-headers", headers);
        return httpClient;
    }

    private static void concatResult(StringBuffer response, HttpMethod method) throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), CHARSET));
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line).append(System.getProperty("line.separator"));
        }
        reader.close();
    }

    public static String doPost(String url, String body) {
        StringBuffer response = new StringBuffer();
        HttpClient httpClient = getHttpClient();
        PostMethod method = new PostMethod(url);
        method.setRequestBody(body);
        try {
            int statusCode = 0;
            if (StringUtils.isNotBlank(body))
            // 对get请求参数做了http请求默认编码，好像没有任何问题，汉字编码后，就成为%式样的字符串
            {
                statusCode = httpClient.executeMethod(method);
            }
            if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_ACCEPTED) {
                concatResult(response, method);
                return response.toString();
            }
        } catch (IOException e) {
            log.error("执行HTTP POST请求" + url + "时，发生异常！", e);
        } finally {
            method.releaseConnection();
        }
        return "failed";
    }


    public static void main(String[] args) throws Exception {

//    String site = "http://172.16.100.38:8080/api/v1/clusters/bigdata/hosts/bd42/host_components/ES_SERVER";
//    String body="{\"HostRoles\":{\"state\":\"STARTED\"}}";
        String site = "http://192.168.31.39:18081/api/v1/clusters/bigdata/hosts/1.associationengine1/host_components/ASSOCIATION_ENGINE_CEP?fields=HostRoles/state";
        String result = doGet(site);
        System.out.println(result);
    }
}
