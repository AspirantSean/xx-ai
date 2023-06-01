package com.tool.asset.api;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.tool.asset.entities.BaasResponse;
import com.tool.asset.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ytm
 * @version 2.0
 * @since 2022/5/26 17:23
 */
@Slf4j
public class BaaSCKServiceApi {

    private final String url;

    private BaaSCKServiceApi(String url) {
        if (StringUtils.isNotBlank(url)) {
            this.url = url;
        } else {
            this.url = "http://1.flink1:8999";
        }
    }

    private static BaaSCKServiceApi baaSCKServiceApi;

    public synchronized static BaaSCKServiceApi getInstance(String url) {
        if (baaSCKServiceApi == null) {
            baaSCKServiceApi = new BaaSCKServiceApi(url);
        }
        return baaSCKServiceApi;
    }

    /**
     * 获取时间间隔内的windowId列表
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public JSONObject getWindowIds(String startTime, String endTime) {
        Map<String, String> header = new HashMap<>();
        header.put("x-forwarded-for", "127.0.0.1");
        header.put("Content-Type", "application/json");
        try {
            return HttpUtil.get(url + "/baas/api/v1/clickhouse/alarm/windowIds", header, new HashMap<>(ImmutableMap.of("startTime", startTime, "endTime", endTime)), JSONObject.class);
        } catch (IOException e) {
            log.error("查询baas windowId列表失败", e);
            throw new RuntimeException(e);
        }
    }

    public BaasResponse getClickhouseQuery(Map<String, Object> params) {
        Map<String, String> header = new HashMap<>();
        header.put("x-forwarded-for", "127.0.0.1");
        try {
            return HttpUtil.post(url + "/baas/api/v1/grammar/clickhouse/query", header, null, params, BaasResponse.class);
        } catch (IOException e) {
            log.error("baas aiql翻译失败", e);
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        BaaSCKServiceApi instance = getInstance("https://192.168.30.195/baasweb");
        JSONObject json = instance.getWindowIds("2023-04-01 00:00:00", "2023-04-02 00:00:00");
        System.out.println(json);
    }
}
