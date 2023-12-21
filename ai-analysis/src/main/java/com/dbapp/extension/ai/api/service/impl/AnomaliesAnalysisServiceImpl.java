package com.dbapp.extension.ai.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dbapp.extension.ai.api.service.IAnomaliesAnalysisService;
import com.dbapp.extension.ai.api.service.IBigdataService;
import com.dbapp.extension.ai.baas.dto.entity.ModelMetric;
import com.dbapp.extension.ai.baas.dto.entity.metric.Metric;
import com.dbapp.extension.ai.baas.dto.entity.model.ai.ModelAI;
import com.dbapp.extension.ai.baas.dto.entity.output.StaticField;
import com.dbapp.extension.ai.mapper.AiModelMapper;
import com.dbapp.extension.ai.utils.FieldGetter;
import com.dbapp.extension.ai.utils.SystemProperUtil;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * AI异常分析
 *
 * @date: 2018年6月29日 上午11:05:12
 */
@Component
public class AnomaliesAnalysisServiceImpl implements IAnomaliesAnalysisService {

    @Resource
    private AiModelMapper aiModelMapper;
    @Resource
    private IBigdataService iBigdataService;

    public static final FieldGetter STATIC_GETTER = staticField(StaticField::getValue);

    /**
     * 对静态字段做处理，如果类型不匹配返回null
     *
     * @param app
     * @param <V>
     * @return
     */
    public static <V> FieldGetter<V> staticField(Function<StaticField, V> app) {
        return f -> {
            if (f instanceof StaticField) {
                return app;
            }
            return null;
        };
    }

    private static final Logger LOG = LoggerFactory.getLogger(AnomaliesAnalysisServiceImpl.class);

    /**
     * 异常点信息字典
     */
    private static final String ANOMALY_DICT_PATH = SystemProperUtil.getResourcesPath() +
            SystemProperUtil.getFileSeparator() + "ai_model_ailpha" +
            SystemProperUtil.getFileSeparator() + "config" +
            SystemProperUtil.getFileSeparator() + "ai_anomaly.json";

    @Override
    public List<Map<String, Object>> sceneSetup(List<Map<String, Object>> sceneList) {

        List<Map<String, Object>> saveOrUpdateScene = new ArrayList<>();

        sceneList.forEach(scene -> {
            String sceneId = (String) scene.get("sceneId");
            String modelId = (String) scene.get("modelId");
            String algorithmId = (String) scene.get("algorithmId");
            Object sceneNo = scene.get("sceneNo");
            if (StringUtils.isBlank(sceneId)) {// 没有sceneId，表示新增的场景
                sceneId = "场景" + sceneNo + "_create_or_update_time:" + System.currentTimeMillis();
                scene.put("sceneId", sceneId);
            }
            if (StringUtils.isNotBlank(modelId) && StringUtils.isNotBlank(algorithmId)) {// 数据齐全才能新增或修改
                saveOrUpdateScene.add(scene);
            }
        });
        this.aiModelMapper.clearAiScene();// 清空场景并重新添加
        if (!saveOrUpdateScene.isEmpty()) {
            this.aiModelMapper.saveOrUpdateScene(saveOrUpdateScene);
        }
        return sceneList();
    }

    @Override
    public List<Map<String, Object>> sceneList() {
        List<Map<String, Object>> scenes = aiModelMapper.listAiScene();
        List<String> modelIds = scenes.stream().map(scene -> (String) scene.get("modelId")).collect(Collectors.toList());
        if (modelIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<ModelMetric> models = iBigdataService.getModelMetricByRuleIds(modelIds);
        if (models == null || models.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> exitModels = models.stream().filter(ModelMetric::isEvent).map(ModelMetric::getRuleId).collect(Collectors.toList());
        return scenes.stream().filter(scene -> exitModels.contains(scene.get("modelId").toString())).collect(Collectors.toList());
    }

    @Override
    public JSONArray sceneInfo(String sceneIds) {
        JSONArray sceneInfos = new JSONArray();
        Arrays.asList(sceneIds.split(",")).forEach(sceneId -> {
            Map<String, Object> aiScene = aiModelMapper.findAISceneAndAlgorithmBySceneId(sceneId);
            ModelAI modelAI = this.iBigdataService.getModelMetric((String) aiScene.get("modelId"));
            JSONArray result = new JSONArray();
            result.add(this.sceneJson(modelAI.getRuleName(), modelAI.getDescription()));// 模型名称、模型描述
            result.add(this.sceneJson((String) aiScene.get("algorithmName"), (String) aiScene.get("content")));// 算法名称、算法介绍
            String patent = (String) aiScene.get("patent");
            if (patent != null && !"".equals(patent)) {
                result.add(this.sceneJson("相关专利", patent));
            }
            String paper = (String) aiScene.get("paper");
            if (paper != null && !"".equals(paper)) {
                result.add(this.sceneJson("相关论文", paper));
            }
            result.add(this.sceneJson("other", (String) aiScene.get("other")));
            sceneInfos.add(result);
        });

        return sceneInfos;
    }

    /**
     * 将场景介绍组成json
     * {
     * **"name":"name",
     * **"description":[
     * ****{
     * ******"content":"descriptionContent",
     * ******"info":""
     * ****}
     * **]
     * }
     *
     * @param name
     * @param description
     * @return
     */
    private JSONObject sceneJson(String name, String description) {

        JSONArray descArr = new JSONArray();
        if (description.trim().startsWith("[") && description.endsWith("]")) {// 如果是专利或者论文则description本身就是json数组
            descArr = JSON.parseArray(description);
        } else {
            JSONObject desc = new JSONObject();
            desc.put("content", description);
            desc.put("info", "");
            descArr.add(desc);
        }
        JSONObject data = new JSONObject();
        data.put("name", name);
        data.put("description", descArr);
        return data;
    }

    @Override
    public JSONObject sceneAnalysisResult(String sceneIds, String startTime, String endTime) {
        LOG.info("*******************ai异常分析结果 start at {} ******************", new Date());
        List<String> ids = new ArrayList<>(Arrays.asList(sceneIds.split(",")));
        List<Map<String, Object>> scenes = this.aiModelMapper.findSceneBySceneIds(ids);
        // 结果数据
        JSONObject analysisData = new JSONObject();
        ExecutorService threadPool = Executors.newCachedThreadPool();
        // 数据后处理
        scenes.forEach(map -> {
            try {
                DataProcessRunable processRunable = new DataProcessRunable(map, analysisData, this.aiModelMapper, iBigdataService, startTime, endTime);
                threadPool.execute(processRunable);
            } catch (Exception e) {
                LOG.error(String.format("ai异常数据后处理异常，异常场景号为：sceneNo = %s", map.get("sceneNo")), e);
            }
        });
        threadPool.shutdown();
        try {
            // 设置最长等待30秒
            threadPool.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("数据处理线程出现异常: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
        LOG.info("*******************ai异常分析结果 end at {} ******************", new Date());
        return analysisData;
    }

    @Override
    public JSONObject anomaliesDetails(String sceneId, @NotNull String time) {
        Map<String, Object> scene = this.aiModelMapper.findSceneBySceneId(sceneId);
        if (scene == null || scene.isEmpty()) {
            return null;
        }
        String algorithmId = (String) scene.get("algorithmId");
        ModelAI aiModel = iBigdataService.getModelMetric((String) scene.get("modelId"));
        String suggest;
        try {
            suggest = (String) STATIC_GETTER.get(aiModel.getOutputParams().getSuggestion());
        } catch (Exception e) {
            suggest = "";
            LOG.warn("AI模型处置建议为空", e);
        }
        Map<String, Object> analysisData = this.aiModelMapper.findAiAnalysisDataByModelIdAndAlgorithmId((String) scene.get("modelId"), algorithmId);
        JSONObject uiData = JSON.parseObject((String) analysisData.get("uiData"));
        // 异常点
        JSONArray anomalies = uiData.getJSONArray("anomalies");
        JSONObject config = uiData.getJSONObject("config");
        JSONObject anomalyInfo = new JSONObject();
        for (Object anomaly : anomalies) {
            if (time.equals(((JSONObject) anomaly).getString("timestamp"))) {
                try {
                    // 异常描述
                    JSONObject anomaliesDescription = new JSONObject();
                    anomaliesDescription.put("name",
                            new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time)));// 2018年06月20日 08:20
                    anomaliesDescription.put("content", ((JSONObject) anomaly).getString("description"));
                    anomalyInfo.put("anomaliesDescription", anomaliesDescription);
                    // 异常指标+训练参数
                    anomalyInfo(anomalyInfo, (JSONObject) anomaly, config);
                    // 辅助信息
                    auxiliaryInfo(anomalyInfo, (JSONObject) anomaly, algorithmId.replace(" ", "").replace("-", ""), config);
                    //可能原因
                    JSONObject reason = new JSONObject();
                    reason.put("name", "异常可能原因");
                    reason.put("content", suggest);
                    anomalyInfo.put("reason", reason);
                    return anomalyInfo;
                } catch (Exception e) {
                    LOG.error("异常详情查询异常", e);
                }
            }
        }
        return anomalyInfo;
    }

    /**
     * 异常点指标
     *
     * @param anomaly
     * @return
     */
    private void anomalyInfo(JSONObject anomalyInfo, JSONObject anomaly, JSONObject config) throws IOException {

        String anomalyDict = FileUtils.readFileToString(new File(ANOMALY_DICT_PATH), StandardCharsets.UTF_8);
        if (StringUtils.isBlank(anomalyDict)) {
            return;
        }
        JSONObject dict = JSON.parseObject(anomalyDict);
        // 异常指标
        JSONObject anomaliesMetrics = new JSONObject(true);
        anomaliesMetrics.put("name", dict.getJSONObject("anomaliesMetrics").getString("name"));
        anomaliesMetrics.put("content", new JSONArray());
        JSONArray keyList = dict.getJSONObject("anomaliesMetrics").getJSONObject("dict").getJSONArray("key");
        JSONArray valueList = dict.getJSONObject("anomaliesMetrics").getJSONObject("dict").getJSONArray("value");
        for (int i = 0; i < keyList.size(); i++) {
            String key = keyList.getString(i);
            if (!anomaly.containsKey(key)) {
                continue;
            }
            JSONObject content = new JSONObject();
            content.put("name", valueList.get(i));
            Object value = anomaly.get(key);
            if ("timestamp".equals(key)) {
                value = ((String) value).substring(0, ((String) value).length() - 3);
            }
            content.put("value", value);
            anomaliesMetrics.getJSONArray("content").add(content);
        }
        anomalyInfo.put("anomaliesMetrics", anomaliesMetrics);
        // 训练参数
        JSONObject algorithmParameters = new JSONObject();
        algorithmParameters.put("name", "算法训练参数");
        algorithmParameters.put("content", new JSONArray());
        keyList = dict.getJSONObject("algorithmParameters").getJSONObject("dict").getJSONArray("key");
        valueList = dict.getJSONObject("algorithmParameters").getJSONObject("dict").getJSONArray("value");
        for (int i = 0; i < keyList.size(); i++) {
            String key = keyList.getString(i);
            if (!config.containsKey(key)) {
                continue;
            }
            JSONObject content = new JSONObject();
            content.put("name", valueList.get(i));
            content.put("value", config.get(key));
            algorithmParameters.getJSONArray("content").add(content);
        }
        anomalyInfo.put("algorithmParameters", algorithmParameters);
    }

    /**
     * 异常点辅助信息
     *
     * @param anomolyInfo
     * @param algorithmId
     * @param anomaly
     * @param config
     */
    private void auxiliaryInfo(JSONObject anomolyInfo, JSONObject anomaly, String algorithmId, JSONObject config) {
        // 辅助展示
        JSONObject auxiliaryInfo = new JSONObject();
        auxiliaryInfo.put("name", "辅助展示");
        JSONObject content = new JSONObject();
        if (algorithmId.equals("WeeklyGaussianEstimation")) {
            JSONObject historyMetrics = new JSONObject();
            historyMetrics.put("name", "历史正常指标");
            historyMetrics.put("xAxis", anomaly.getJSONObject("trainningData").getJSONArray("timestampData"));
            historyMetrics.put("yAxis", anomaly.getJSONObject("trainningData").getJSONArray("realData"));
            content.put("historyMetrics", historyMetrics);

            JSONObject gaussianDistribution = new JSONObject();
            gaussianDistribution.put("name", "高斯分布图");
            gaussianDistribution.put("fit", anomaly.get("fit"));
            gaussianDistribution.put("stdThreshold", config.get("stdThreshold"));
            gaussianDistribution.put("sigma", anomaly.get("sigma"));
            gaussianDistribution.put("std", anomaly.get("std"));
            content.put("GaussianDistribution", gaussianDistribution);
            auxiliaryInfo.put("content", content);
            anomolyInfo.put("auxiliaryInfo", auxiliaryInfo);
        } else if (algorithmId.equals("RPCASST")) {
            JSONObject eigenvectorMatrix = new JSONObject();
            eigenvectorMatrix.put("name", "特征向量矩阵");
            eigenvectorMatrix.put("PCAcomponentNum", anomaly.get("PCAcomponentNum"));
            eigenvectorMatrix.put("PCAenergyProportion", anomaly.get("PCAenergyProportion"));
            eigenvectorMatrix.put("PCAeigenValues", anomaly.get("PCAeigenValues"));
            content.put("eigenvectorMatrix", eigenvectorMatrix);
            auxiliaryInfo.put("content", content);
            anomolyInfo.put("auxiliaryInfo", auxiliaryInfo);
        }
    }

    @Override
    public JSONArray modelAndAlgorithmList() {
        List<ModelAI> aiModels = iBigdataService.getAllEnableAIModels();
        JSONArray result = new JSONArray();
        // 存放算法id(去重) 用于查询算法
        Set<String> algorithmIdSet = new HashSet<>();
        for (ModelAI aiModel : aiModels) {
            JSONObject aiModelJson = new JSONObject();// 存放模型json
            String[] algorithmIds = aiModel.getDetectionParams().getAlgorithm();
            algorithmIdSet.addAll(Arrays.asList(algorithmIds));
            aiModelJson.put("modelId", aiModel.getRuleId());
            aiModelJson.put("modelName", aiModel.getRuleName());
            aiModelJson.put("algorithm", algorithmIds);// 用于将查询到的对应算法放入模型
            result.add(aiModelJson);
        }
        List<Map<String, Object>> algorithmList = aiModelMapper.listAiAnalysisAlgorithm(new ArrayList<>(algorithmIdSet));
        if (!algorithmList.isEmpty()) {
            Map<String, Map<String, Object>> algorithmMap = algorithmList.stream().collect(Collectors.toMap(o -> (String) o.get("algorithmId"), o -> o));
            for (JSONObject aiModel : result.toJavaList(JSONObject.class)) {
                JSONArray algorithmIds = aiModel.getJSONArray("algorithm");
                List<Map<String, Object>> algorithms = algorithmIds.toJavaList(String.class)
                        .stream()
                        .map(algorithmMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                aiModel.put("algorithm", algorithms);
            }
        }
        return result;
    }

    /**
     * 根据模型ID查询指标值
     *
     * @param sceneId
     * @return
     */
    @Override
    public JSONObject getMetricByModelId(String sceneId, String time) {
        try {
            Map<String, Object> scene = this.aiModelMapper.findSceneBySceneId(sceneId);
            if (scene == null || scene.isEmpty()) {
                return null;
            }
            ModelAI aiModel = iBigdataService.getModelMetric((String) scene.get("modelId"));
            String metricId = aiModel.getDetectionParams().getMetric();
            Metric metric = iBigdataService.getModelMetric(metricId);
            JSONObject result = new JSONObject();
            result.put("metric", metric);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(time));
            // 当前写死前后10分钟
            calendar.add(Calendar.MINUTE, -10);
            result.put("start", sdf.format(calendar.getTime()));
            calendar.add(Calendar.MINUTE, 20);
            result.put("end", sdf.format(calendar.getTime()));
            return result;
        } catch (ParseException e) {
            LOG.error(String.format("ai模型-时间解析错误，time=%s", time), e);
        } catch (Exception e) {
            LOG.error(String.format("ai模型-指标查询错误，sceneId=%s", sceneId), e);
        }
        return new JSONObject();
    }

}
