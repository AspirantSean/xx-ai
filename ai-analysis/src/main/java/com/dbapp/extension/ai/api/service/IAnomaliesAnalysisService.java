package com.dbapp.extension.ai.api.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * AI异常分析
 *
 * @date: 2018年6月29日 上午11:05:12
 */
public interface IAnomaliesAnalysisService {

    List<Map<String, Object>> sceneSetup(List<Map<String, Object>> sceneList);

    List<Map<String, Object>> sceneList();

    JSONArray sceneInfo(String sceneIds);

    JSONObject sceneAnalysisResult(String sceneIds, String startTime, String endTime);

    JSONObject anomaliesDetails(String sceneId, String time);

    JSONArray modelAndAlgorithmList();

    JSONObject getMetricByModelId(String sceneId, String time);

}
