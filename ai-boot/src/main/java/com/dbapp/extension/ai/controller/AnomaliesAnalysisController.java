package com.dbapp.extension.ai.controller;


import com.alibaba.fastjson.JSONObject;
import com.dbapp.extension.ai.api.service.IAnomaliesAnalysisService;
import com.google.common.collect.ImmutableMap;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * AI异常分析
 *
 * @author: tianming.yu
 * @date: 2018年6月29日 上午11:05:12
 */

@RestController
@RequestMapping(value = "/api/aiAnomaliesAnalysis")
public class AnomaliesAnalysisController {

    private static final Logger LOG = LoggerFactory.getLogger(AnomaliesAnalysisController.class);

    @Resource
    private IAnomaliesAnalysisService service;

    @PreAuthorize("hasAuthority('AI_analysis-Edit')")
    @PostMapping(value = "/sceneSetup")
    @ResponseBody
//    @OpLog(module = AI_analysis, message = "{message}", op_Type = OpType.SAVE, describe = "AI模型分析场景配置")
    public Object sceneSetup(@RequestBody List<Map<String, Object>> sceneList) {

        try {
            List<Map<String, Object>> result = this.service.sceneSetup(sceneList);
            StringBuilder message = new StringBuilder();
            for (Map<String, Object> map : result) {
                message.append("场景id=").append(map.get("sceneId")).append(";");
            }
//            OPLogContext.put("message", "场景设置成功！当前场景: " + message);
            return ImmutableMap.of(
                    "code", 0,
                    "data", result,
                    "msg", "success"
            );
        } catch (Exception e) {
            LOG.error("场景设置失败！" + e.getMessage(), e);
//            OPLogContext.put("message", "场景设置失败！");
            return ImmutableMap.of(
                    "code", -1,
                    "msg", "failed: 场景查询失败！"
            );
        }
    }

    @PreAuthorize("hasAuthority('AI_analysis-Search')")
    @GetMapping(value = "/sceneList")
    @ResponseBody
    public Object sceneList() {

        try {
            Object result = this.service.sceneList();
            return ImmutableMap.of(
                    "code", 0,
                    "data", result,
                    "msg", "success"
            );
        } catch (Exception e) {
            LOG.error("场景查询失败！" + e.getMessage(), e);
            return ImmutableMap.of(
                    "code", -1,
                    "msg", "failed: 场景查询失败！"
            );
        }
    }

    @PreAuthorize("hasAuthority('AI_analysis-Search')")
    @GetMapping(value = "/sceneInfo")
    @ResponseBody
    public Object sceneInfo(String sceneIds) {

        try {
            Object result = this.service.sceneInfo(sceneIds);
            return ImmutableMap.of(
                    "code", 0,
                    "data", result,
                    "msg", "success"
            );
        } catch (Exception e) {
            LOG.error("查询场景介绍失败！" + e.getMessage(), e);
            return ImmutableMap.of(
                    "code", -1,
                    "msg", "failed: 查询场景介绍失败！"
            );
        }
    }

    @PreAuthorize("hasAuthority('AI_analysis-Search')")
    @GetMapping(value = "/sceneAnalysisResult")
    @ResponseBody
    public Object sceneAnalysisResult(String sceneIds, String startTime, String endTime) {

        try {
            Object result = this.service.sceneAnalysisResult(sceneIds, startTime, endTime);
            return ImmutableMap.of(
                    "code", 0,
                    "data", result,
                    "msg", "success"
            );
        } catch (Exception e) {
            LOG.error("异常分析结果查询失败！" + e.getMessage(), e);
            return ImmutableMap.of(
                    "code", -1,
                    "msg", "failed: 异常分析结果查询失败！"
            );
        }
    }
    @PreAuthorize("hasAuthority('AI_analysis-Search')")
    @GetMapping(value = "/anomaliesDetails")
    @ResponseBody
    public Object anomaliesDetails(String sceneId, String time) {

        try {
            Object result = this.service.anomaliesDetails(sceneId, time);
            return ImmutableMap.of(
                    "code", 0,
                    "data", result,
                    "msg", "success"
            );
        } catch (Exception e) {
            LOG.error("异常点详情查询失败！" + e.getMessage(), e);
            return ImmutableMap.of(
                    "code", -1,
                    "msg", "failed: 异常点详情查询失败！"
            );
        }
    }

    @PreAuthorize("hasAuthority('AI_analysis-Search')")
    @GetMapping(value = "/modelAndAlgorithmList")
    @ResponseBody
    public Object modelAndAlgorithmList() {

        try {
            Object result = this.service.modelAndAlgorithmList();
            return ImmutableMap.of(
                    "code", 0,
                    "data", result,
                    "msg", "success"
            );
        } catch (Exception e) {
            LOG.error("查询模型列表失败！" + e.getMessage(), e);
            return ImmutableMap.of(
                    "code", -1,
                    "msg", "failed: 查询模型列表失败！"
            );
        }
    }

    @PreAuthorize("hasAuthority('AI_analysis-Search')")
    @GetMapping(value = "/findMetricBySceneId")
    @ResponseBody
    public Object getMetricById(String sceneId, String time) {

        try {
            JSONObject result = this.service.getMetricByModelId(sceneId, time);
            return ImmutableMap.of(
                    "code", 0,
                    "data", result,
                    "msg", "success"
            );
        } catch (Exception e) {
            LOG.error("AI异常分析--指标查询失败！" + e.getMessage(), e);
            return ImmutableMap.of(
                    "code", -1,
                    "msg", "failed: 查询模型列表失败！"
            );
        }
    }

}
